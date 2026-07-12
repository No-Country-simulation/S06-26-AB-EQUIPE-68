package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.Curso;
import com.bitsystem.bitapp.domain.InscricaoCurso;
import com.bitsystem.bitapp.dto.CursoDto;
import com.bitsystem.bitapp.dto.InscricaoCursoDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.repository.CursoRepository;
import com.bitsystem.bitapp.repository.InscricaoCursoRepository;
import com.bitsystem.bitapp.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CursoService {

    private static final Logger log = LoggerFactory.getLogger(CursoService.class);

    private final CursoRepository cursoRepository;
    private final InscricaoCursoRepository inscricaoRepository;
    private final UserRepository userRepository;

    public CursoService(CursoRepository cursoRepository,
                        InscricaoCursoRepository inscricaoRepository,
                        UserRepository userRepository) {
        this.cursoRepository = cursoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.userRepository = userRepository;
    }

    /**
     * Inscreve o usuário no curso, evitando duplicidade (mesmo usuarioId+cursoId).
     * usuarioId vem do payload; se ausente, deriva do usuário autenticado (JWT).
     */
    public InscricaoCursoDto.Response inscrever(InscricaoCursoDto.Request request, String emailAutenticado) {
        Long usuarioId = request.usuarioId();
        if (usuarioId == null || usuarioId <= 0) {
            usuarioId = userRepository.findByEmail(emailAutenticado)
                    .map(u -> u.getId())
                    .orElseThrow(() -> new BusinessException("USUARIO_NAO_IDENTIFICADO",
                            "Não foi possível identificar o usuário da inscrição."));
        }

        Long cursoId = request.cursoId();
        cursoRepository.findById(cursoId)
                .orElseThrow(() -> new BusinessException("CURSO_NAO_ENCONTRADO", "Curso não encontrado: " + cursoId));

        Optional<InscricaoCurso> existente = inscricaoRepository.findByUsuarioIdAndCursoId(usuarioId, cursoId);
        if (existente.isPresent()) {
            log.info("[CursoService] Inscrição já existente: usuarioId={}, cursoId={}", usuarioId, cursoId);
            return InscricaoCursoDto.Response.from(existente.get(), true);
        }

        InscricaoCurso nova = InscricaoCurso.builder()
                .usuarioId(usuarioId)
                .cursoId(cursoId)
                .status("INSCRITO")
                .build();
        nova = inscricaoRepository.save(nova);
        log.info("[CursoService] Inscrição criada: id={}, usuarioId={}, cursoId={}", nova.getId(), usuarioId, cursoId);
        return InscricaoCursoDto.Response.from(nova, false);
    }

    public List<CursoDto> listarTodas() {
        return cursoRepository.findByAtivaTrue().stream()
                .map(CursoDto::from)
                .collect(Collectors.toList());
    }

    public CursoDto buscarPorId(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado: " + id));
        return CursoDto.from(curso);
    }

    public List<CursoDto> buscarComFiltros(String q, String regiao, String area,
                                            String modalidade, Boolean gratuito, String nivel) {
        String qN = (q == null || q.isBlank()) ? null : q.trim();
        String rN = (regiao == null || regiao.isBlank()) ? null : regiao.trim();
        String aN = (area == null || area.isBlank()) ? null : area.trim();
        String mN = (modalidade == null || modalidade.isBlank()) ? null : modalidade.trim();
        String nN = (nivel == null || nivel.isBlank()) ? null : nivel.trim();

        return cursoRepository.buscarComFiltros(qN, rN, aN, mN, gratuito, nN).stream()
                .map(CursoDto::from)
                .collect(Collectors.toList());
    }

    public List<CursoDto> listarGratuitos() {
        return cursoRepository.findGratuitos().stream()
                .map(CursoDto::from)
                .collect(Collectors.toList());
    }

    public List<CursoDto> listarBeneficentes() {
        return cursoRepository.findBeneficentes().stream()
                .map(CursoDto::from)
                .collect(Collectors.toList());
    }

    public List<String> listarRegioes() {
        return cursoRepository.findByAtivaTrue().stream()
                .map(Curso::getRegiao)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public long contarCursos() {
        return cursoRepository.findByAtivaTrue().size();
    }

    public Curso salvar(Curso curso) {
        return cursoRepository.save(curso);
    }

    public void salvarTodas(List<Curso> cursos) {
        cursoRepository.saveAll(cursos);
    }
}
