package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.Curso;
import com.bitsystem.bitapp.dto.CursoDto;
import com.bitsystem.bitapp.repository.CursoRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
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
