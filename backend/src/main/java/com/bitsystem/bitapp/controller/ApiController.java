package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.*;
import com.bitsystem.bitapp.service.AuthService;
import com.bitsystem.bitapp.service.GeolocationService;
import com.bitsystem.bitapp.service.OrientacaoService;
import com.bitsystem.bitapp.service.SaudeMentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final OrientacaoService orientacaoService;
    private final SaudeMentalService saudeMentalService;
    private final GeolocationService geolocationService;
    private final AuthService authService;

    public ApiController(
            OrientacaoService orientacaoService,
            SaudeMentalService saudeMentalService,
            GeolocationService geolocationService,
            AuthService authService) {
        this.orientacaoService = orientacaoService;
        this.saudeMentalService = saudeMentalService;
        this.geolocationService = geolocationService;
        this.authService = authService;
    }

    // ────────────────────────────────────────────────────────────────────────
    // DESATIVADO (2026-07): o Dashboard passou a usar o Assessment Agent do Tiago
    // (POST /api/assessment). O endpoint /api/orientar (Gemini direto) fica
    // comentado para retomada futura — não deve ser chamado nem atrapalhar.
    // O bean OrientacaoService segue existindo, porém sem rota exposta.
    // ────────────────────────────────────────────────────────────────────────
    // @PostMapping("/orientar")
    // public ResponseEntity<StandardApiResponse<OrientacaoDto.Response>> orientar(
    //         @RequestBody @Valid OrientacaoDto.Request request) {
    //     return ResponseEntity.ok(StandardApiResponse.ok(orientacaoService.processarOrientacao(request)));
    // }

    @PostMapping("/saude")
    public ResponseEntity<StandardApiResponse<SaudeDto.Response>> verificarSaude(
            @RequestBody @Valid SaudeDto.Request request) {
        return ResponseEntity.ok(StandardApiResponse.ok(saudeMentalService.avaliarEstadoMental(request)));
    }

    @GetMapping("/saude/historico")
    public ResponseEntity<StandardApiResponse<java.util.List<SaudeDto.HistoricoResponse>>> historicoSaude(
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(StandardApiResponse.ok(saudeMentalService.buscarHistorico(usuarioId)));
    }

    @GetMapping("/network-status/{usuarioId}")
    public ResponseEntity<StandardApiResponse<NetworkStatusDto>> getNetworkStatus(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "5000") double raioMetros) {
        return ResponseEntity.ok(StandardApiResponse.ok(geolocationService.getNetworkStatus(usuarioId, raioMetros)));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<StandardApiResponse<java.util.List<UsuarioDto.Response>>> listarUsuarios() {
        java.util.List<UsuarioDto.Response> response = authService.listUsers().stream()
                .map(user -> new UsuarioDto.Response(
                        user.id(), user.nome(), user.email(),
                        user.cidade(), user.whatsapp(),
                        user.nivelProfissional(), user.areaTecnologia(),
                        user.competenciasAtuais()))
                .toList();
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }
}
