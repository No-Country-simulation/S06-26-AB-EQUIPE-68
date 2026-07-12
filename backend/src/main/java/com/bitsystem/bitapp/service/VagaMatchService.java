package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.User;
import com.bitsystem.bitapp.domain.Vaga;
import com.bitsystem.bitapp.dto.VagaMatchDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.repository.UserRepository;
import com.bitsystem.bitapp.repository.VagaRepository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    /** Conteúdo entre parênteses, capturado para virar tokens adicionais. */
    private static final Pattern PARENTESES = Pattern.compile("\\(([^()]*)\\)");

    /** Separadores dentro de um fragmento: barra, ou " e "/" ou " (exigem espaço nos dois lados). */
    private static final Pattern SEPARADOR_FRAGMENTO =
        Pattern.compile("\\s*/\\s*|\\s+e\\s+|\\s+ou\\s+", Pattern.CASE_INSENSITIVE);

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

        Set<String> canonicosUsuario = expandirPalavras(splitLista(competenciasUsuario)).stream()
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
        List<String> tokens = new ArrayList<>();
        for (String item : raw.split(",")) {
            tokens.addAll(tokenizarItem(item));
        }
        return tokens;
    }

    /**
     * Quebra um item (já separado por vírgula) em tokens: o texto fora dos
     * parênteses vira um token, e o conteúdo de cada parênteses vira token(s)
     * adicionais — depois cada fragmento ainda é quebrado por "/", " e " e " ou ".
     * Ex.: "Java SE (Core Java)" -> ["Java SE", "Core Java"]
     *      "Banco de Dados (PostgreSQL/MySQL)" -> ["Banco de Dados", "PostgreSQL", "MySQL"]
     */
    private static List<String> tokenizarItem(String item) {
        List<String> fragmentos = new ArrayList<>();
        Matcher matcher = PARENTESES.matcher(item);
        StringBuilder fora = new StringBuilder();
        int ultimoFim = 0;
        while (matcher.find()) {
            fora.append(item, ultimoFim, matcher.start());
            fragmentos.add(matcher.group(1));
            ultimoFim = matcher.end();
        }
        fora.append(item.substring(ultimoFim));
        fragmentos.add(0, fora.toString());

        List<String> tokens = new ArrayList<>();
        for (String fragmento : fragmentos) {
            for (String sub : SEPARADOR_FRAGMENTO.split(fragmento)) {
                String token = sub.trim();
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
            }
        }
        return tokens;
    }

    /**
     * Adiciona, para cada token composto (ex.: "Java SE", "Core Java"), também
     * suas palavras individuais como tokens extras — só assim um item de perfil
     * como "Java SE (Core Java)" bate com uma tecnologia curta de vaga ("Java").
     * Aplicado só ao lado do usuário: o lado da vaga precisa manter os tokens
     * originais intactos, pois eles compõem a lista exibida em atendidas/faltantes.
     */
    private static List<String> expandirPalavras(List<String> tokens) {
        List<String> expandido = new ArrayList<>(tokens);
        for (String token : tokens) {
            if (token.contains(" ")) {
                for (String palavra : token.split("\\s+")) {
                    if (!palavra.isBlank()) {
                        expandido.add(palavra);
                    }
                }
            }
        }
        return expandido;
    }

    /** Normaliza (lowercase, trim, sem acentos) e resolve sinônimos fixos. */
    static String canonicalizar(String termo) {
        String semAcento = Normalizer.normalize(termo.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return SINONIMOS.getOrDefault(semAcento, semAcento);
    }
}
