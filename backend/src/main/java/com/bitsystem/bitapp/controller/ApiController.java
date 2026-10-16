package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.config.UsuarioDoToken;
import com.bitsystem.bitapp.dto.*;
import com.bitsystem.bitapp.service.AuthService;
import com.bitsystem.bitapp.service.GeolocationService;
import com.bitsystem.bitapp.service.OrientacaoService;
import com.bitsystem.bitapp.service.SaudeMentalService;
import com.bitsystem.bitapp.service.SugestaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final OrientacaoService orientacaoService;
    private final SaudeMentalService saudeMentalService;
    private final GeolocationService geolocationService;
    private final AuthService authService;
    private final SugestaoService sugestaoService;

    public ApiController(
            OrientacaoService orientacaoService,
            SaudeMentalService saudeMentalService,
            GeolocationService geolocationService,
            AuthService authService,
            SugestaoService sugestaoService) {
        this.orientacaoService = orientacaoService;
        this.saudeMentalService = saudeMentalService;
        this.geolocationService = geolocationService;
        this.authService = authService;
        this.sugestaoService = sugestaoService;
    }

    @PostMapping("/orientar")
    public ResponseEntity<StandardApiResponse<OrientacaoDto.Response>> orientar(
            @RequestBody @Valid OrientacaoDto.Request request,
            Authentication authentication) {
        Long usuarioId = UsuarioDoToken.resolver(authentication, request.usuarioId());
        OrientacaoDto.Request doDono = new OrientacaoDto.Request(
                usuarioId, request.perfil(), request.nivel(), request.regiao(),
                request.idioma(), request.lat(), request.lng());
        return ResponseEntity.ok(StandardApiResponse.ok(orientacaoService.processarOrientacao(doDono)));
    }

    @PostMapping("/saude")
    public ResponseEntity<StandardApiResponse<SaudeDto.Response>> verificarSaude(
            @RequestBody @Valid SaudeDto.Request request,
            Authentication authentication) {
        Long usuarioId = UsuarioDoToken.resolver(authentication, request.usuarioId());
        SaudeDto.Request doDono = new SaudeDto.Request(
                usuarioId, request.nota(), request.contexto(), request.idioma());
        return ResponseEntity.ok(StandardApiResponse.ok(saudeMentalService.avaliarEstadoMental(doDono)));
    }

    @GetMapping("/saude/historico")
    public ResponseEntity<StandardApiResponse<java.util.List<SaudeDto.HistoricoResponse>>> historicoSaude(
            @RequestParam(required = false) Long usuarioId,
            Authentication authentication) {
        Long donoId = UsuarioDoToken.resolver(authentication, usuarioId);
        return ResponseEntity.ok(StandardApiResponse.ok(saudeMentalService.buscarHistorico(donoId)));
    }

    @GetMapping("/network-status/{usuarioId}")
    public ResponseEntity<StandardApiResponse<NetworkStatusDto>> getNetworkStatus(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "5000") double raioMetros,
            Authentication authentication) {
        Long donoId = UsuarioDoToken.resolver(authentication, usuarioId);
        return ResponseEntity.ok(StandardApiResponse.ok(geolocationService.getNetworkStatus(donoId, raioMetros)));
    }

    @PutMapping("/usuarios/{id}/localizacao")
    public ResponseEntity<StandardApiResponse<UsuarioDto.LocalizacaoResponse>> atualizarLocalizacao(
            @PathVariable Long id,
            Authentication authentication,
            @RequestBody @Valid UsuarioDto.LocalizacaoRequest request) {
        UsuarioDto.LocalizacaoResponse response = authService.atualizarLocalizacao(
                authentication.getName(), id, request.latitude(), request.longitude());
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }

    @GetMapping("/sugestoes/{usuarioId}")
    public ResponseEntity<StandardApiResponse<SugestaoDto.Response>> sugestoes(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "pt") String idioma,
            Authentication authentication) {
        Long donoId = UsuarioDoToken.resolver(authentication, usuarioId);
        return ResponseEntity.ok(StandardApiResponse.ok(sugestaoService.gerarSugestoes(donoId, idioma)));
    }
}
