package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.dto.CursoDto;
import com.bitsystem.bitapp.service.CursoService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public ResponseEntity<StandardApiResponse<List<CursoDto>>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String regiao,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String modalidade,
            @RequestParam(required = false) Boolean gratuito,
            @RequestParam(required = false) String nivel) {

        List<CursoDto> cursos = cursoService.buscarComFiltros(q, regiao, area, modalidade, gratuito, nivel);
        return ResponseEntity.ok(StandardApiResponse.ok(cursos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardApiResponse<CursoDto>> buscarPorId(@PathVariable Long id) {
        CursoDto curso = cursoService.buscarPorId(id);
        return ResponseEntity.ok(StandardApiResponse.ok(curso));
    }

    @GetMapping("/regioes")
    public ResponseEntity<StandardApiResponse<List<String>>> listarRegioes() {
        List<String> regioes = cursoService.listarRegioes();
        return ResponseEntity.ok(StandardApiResponse.ok(regioes));
    }

    @GetMapping("/gratuitos")
    public ResponseEntity<StandardApiResponse<List<CursoDto>>> listarGratuitos() {
        List<CursoDto> cursos = cursoService.listarGratuitos();
        return ResponseEntity.ok(StandardApiResponse.ok(cursos));
    }

    @GetMapping("/beneficentes")
    public ResponseEntity<StandardApiResponse<List<CursoDto>>> listarBeneficentes() {
        List<CursoDto> cursos = cursoService.listarBeneficentes();
        return ResponseEntity.ok(StandardApiResponse.ok(cursos));
    }

    @GetMapping("/stats")
    public ResponseEntity<StandardApiResponse<CursoStats>> stats() {
        long total = cursoService.contarCursos();
        long gratuitos = cursoService.listarGratuitos().size();
        List<String> regioes = cursoService.listarRegioes();
        return ResponseEntity.ok(StandardApiResponse.ok(new CursoStats(total, gratuitos, regioes.size())));
    }

    public record CursoStats(long total, long gratuitos, long regioes) {}
}
