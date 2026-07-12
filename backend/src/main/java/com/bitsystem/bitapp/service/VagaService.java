package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.CandidaturaVaga;
import com.bitsystem.bitapp.domain.User;
import com.bitsystem.bitapp.domain.Vaga;
import com.bitsystem.bitapp.dto.CandidaturaVagaDto;
import com.bitsystem.bitapp.dto.VagaDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.repository.CandidaturaVagaRepository;
import com.bitsystem.bitapp.repository.UserRepository;
import com.bitsystem.bitapp.repository.VagaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VagaService {

    private static final Logger log = LoggerFactory.getLogger(VagaService.class);

    private final VagaRepository vagaRepository;
    private final CandidaturaVagaRepository candidaturaRepository;
    private final UserRepository userRepository;

    public VagaService(VagaRepository vagaRepository,
                       CandidaturaVagaRepository candidaturaRepository,
                       UserRepository userRepository) {
        this.vagaRepository = vagaRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.userRepository = userRepository;
    }

    /**
     * Persiste o envio de currículo para a vaga. usuarioId vem do payload;
     * se ausente, deriva do usuário autenticado (JWT). Nome/e-mail são
     * completados a partir do perfil quando não vierem no payload.
     */
    public CandidaturaVagaDto.Response enviarCurriculo(CandidaturaVagaDto.Request request, String emailAutenticado) {
        Optional<User> autenticado = emailAutenticado != null
                ? userRepository.findByEmail(emailAutenticado)
                : Optional.empty();

        Long usuarioId = request.usuarioId();
        if (usuarioId == null || usuarioId <= 0) {
            usuarioId = autenticado
                    .map(User::getId)
                    .orElseThrow(() -> new BusinessException("USUARIO_NAO_IDENTIFICADO",
                            "Não foi possível identificar o usuário da candidatura."));
        }

        Long vagaId = request.vagaId();
        vagaRepository.findById(vagaId)
                .orElseThrow(() -> new BusinessException("VAGA_NAO_ENCONTRADA", "Vaga não encontrada: " + vagaId));

        String nome = request.nome() != null && !request.nome().isBlank()
                ? request.nome()
                : autenticado.map(User::getNome).orElse(null);
        String email = request.email() != null && !request.email().isBlank()
                ? request.email()
                : autenticado.map(User::getEmail).orElse(null);

        CandidaturaVaga candidatura = CandidaturaVaga.builder()
                .usuarioId(usuarioId)
                .vagaId(vagaId)
                .nome(nome)
                .email(email)
                .mensagem(request.mensagem())
                .status("ENVIADA")
                .build();
        candidatura = candidaturaRepository.save(candidatura);
        log.info("[VagaService] Candidatura registrada: id={}, usuarioId={}, vagaId={}",
                candidatura.getId(), usuarioId, vagaId);
        return CandidaturaVagaDto.Response.from(candidatura);
    }

    public List<VagaDto> listarTodas() {
        return vagaRepository.findByAtivaTrue().stream()
                .map(VagaDto::from)
                .collect(Collectors.toList());
    }

    public VagaDto buscarPorId(Long id) {
        Vaga vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada: " + id));
        return VagaDto.from(vaga);
    }

    public List<VagaDto> buscarComFiltros(String q, String regiao, String nivel, String area, String contrato) {
        String qNorm = (q == null || q.isBlank()) ? null : q.trim();
        String rNorm = (regiao == null || regiao.isBlank()) ? null : regiao.trim();
        String nNorm = (nivel == null || nivel.isBlank()) ? null : nivel.trim();
        String aNorm = (area == null || area.isBlank()) ? null : area.trim();
        String cNorm = (contrato == null || contrato.isBlank()) ? null : contrato.trim();

        return vagaRepository.buscarComFiltros(qNorm, rNorm, nNorm, aNorm, cNorm).stream()
                .map(VagaDto::from)
                .collect(Collectors.toList());
    }

    public List<String> listarRegioes() {
        return vagaRepository.findByAtivaTrue().stream()
                .map(Vaga::getRegiao)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public long contarVagas() {
        return vagaRepository.findByAtivaTrue().size();
    }

    public Vaga salvar(Vaga vaga) {
        return vagaRepository.save(vaga);
    }

    public void salvarTodas(List<Vaga> vagas) {
        vagaRepository.saveAll(vagas);
    }
}
