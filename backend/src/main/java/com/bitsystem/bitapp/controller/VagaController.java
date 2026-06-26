package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.dto.VagaDto;
import com.bitsystem.bitapp.service.VagaService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vagas")
public class VagaController {

    private final VagaService vagaService;

    public VagaController(VagaService vagaService) {
        this.vagaService = vagaService;
    }

    @GetMapping
    public ResponseEntity<StandardApiResponse<List<VagaDto>>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String regiao,
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String contrato) {

        List<VagaDto> vagas = vagaService.buscarComFiltros(q, regiao, nivel, area, contrato);
        return ResponseEntity.ok(StandardApiResponse.ok(vagas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardApiResponse<VagaDto>> buscarPorId(@PathVariable Long id) {
        VagaDto vaga = vagaService.buscarPorId(id);
        return ResponseEntity.ok(StandardApiResponse.ok(vaga));
    }

    @GetMapping("/regioes")
    public ResponseEntity<StandardApiResponse<List<String>>> listarRegioes() {
        List<String> regioes = vagaService.listarRegioes();
        return ResponseEntity.ok(StandardApiResponse.ok(regioes));
    }

    @GetMapping("/stats")
    public ResponseEntity<StandardApiResponse<VagaStats>> stats() {
        long total = vagaService.contarVagas();
        List<String> regioes = vagaService.listarRegioes();
        return ResponseEntity.ok(StandardApiResponse.ok(new VagaStats(total, regioes.size())));
    }

    public record VagaStats(long total, long regioes) {}
}
