package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.User;
import com.bitsystem.bitapp.domain.Vaga;
import com.bitsystem.bitapp.dto.VagaMatchDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.repository.UserRepository;
import com.bitsystem.bitapp.repository.VagaRepository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Calcula o percentual de aderência entre as competências de um usuário e as
 * tecnologias exigidas por uma vaga. O número é 100% determinístico — interseção
 * de conjuntos normalizados em Java. A IA nunca participa deste cálculo, só do
 * "como resolver" (ver ComoResolverService).
 */
@Service
public class VagaMatchService {

    /**
     * Sinônimos mínimos: forma alternativa normalizada -> forma canônica.
     * Ambos os lados da comparação passam por {@link #canonicalizar}, então
     * "Spring Boot" (usuário) e "Spring" (vaga) convergem para "spring".
     */
    static final Map<String, String> SINONIMOS = Map.of(
        "spring boot", "spring",
        "js", "javascript",
        "postgres", "postgresql",
        "reactjs", "react"
    );

    private final UserRepository userRepository;
    private final VagaRepository vagaRepository;

    public VagaMatchService(UserRepository userRepository, VagaRepository vagaRepository) {
        this.userRepository = userRepository;
        this.vagaRepository = vagaRepository;
    }

    public VagaMatchDto.Resultado calcularMatchPorId(Long vagaId, Long usuarioId) {
        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new BusinessException("VAGA_NAO_ENCONTRADA", "Vaga não encontrada: " + vagaId));
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado: " + usuarioId));
        return calcular(usuario.getCompetenciasAtuais(), vaga.getTecnologias());
    }

    /** Match de todas as vagas ativas para um usuário — usado na listagem em lote (sem IA). */
    public List<VagaMatchDto.LoteItem> calcularMatchLote(Long usuarioId) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado: " + usuarioId));
        return vagaRepository.findByAtivaTrue().stream()
                .map(vaga -> new VagaMatchDto.LoteItem(
                        vaga.getId(),
                        calcular(usuario.getCompetenciasAtuais(), vaga.getTecnologias()).matchPercentual()))
                .collect(Collectors.toList());
    }

    /**
     * Cálculo puro: interseção de skills do usuário x tecnologias da vaga.
     * Vaga sem tecnologias cadastradas retorna matchPercentual null — ausência
     * de dado não é incompatibilidade.
     */
    public VagaMatchDto.Resultado calcular(String competenciasUsuario, String tecnologiasVaga) {
        List<String> tecnologias = splitLista(tecnologiasVaga);
        if (tecnologias.isEmpty()) {
            return new VagaMatchDto.Resultado(null, List.of(), List.of());
        }

        Set<String> canonicosUsuario = splitLista(competenciasUsuario).stream()
                .map(VagaMatchService::canonicalizar)
                .collect(Collectors.toSet());

        List<String> atendidas = new ArrayList<>();
        List<String> faltantes = new ArrayList<>();
        for (String tecnologia : tecnologias) {
            if (canonicosUsuario.contains(canonicalizar(tecnologia))) {
                atendidas.add(tecnologia);
            } else {
                faltantes.add(tecnologia);
            }
        }

        int percentual = Math.round(100f * atendidas.size() / tecnologias.size());
        return new VagaMatchDto.Resultado(percentual, atendidas, faltantes);
    }

    private static List<String> splitLista(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /** Normaliza (lowercase, trim, sem acentos) e resolve sinônimos fixos. */
    static String canonicalizar(String termo) {
        String semAcento = Normalizer.normalize(termo.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return SINONIMOS.getOrDefault(semAcento, semAcento);
    }
}
