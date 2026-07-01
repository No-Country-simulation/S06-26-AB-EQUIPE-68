package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.CandidaturaVagaDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.dto.VagaDto;
import com.bitsystem.bitapp.service.VagaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    // Ação de usuário logado — cai em anyRequest().authenticated() (POST não é liberado).
    @PostMapping("/enviar-curriculo")
    public ResponseEntity<StandardApiResponse<CandidaturaVagaDto.Response>> enviarCurriculo(
            @RequestBody @Valid CandidaturaVagaDto.Request request,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        CandidaturaVagaDto.Response response = vagaService.enviarCurriculo(request, email);
        return ResponseEntity.ok(StandardApiResponse.ok(response));
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
