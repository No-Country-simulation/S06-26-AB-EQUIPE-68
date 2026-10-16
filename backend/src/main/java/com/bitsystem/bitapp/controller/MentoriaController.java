package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.config.UsuarioDoToken;
import com.bitsystem.bitapp.dto.SolicitacaoMentoriaDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.service.MentoriaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentorias")
public class MentoriaController {

    private final MentoriaService mentoriaService;

    public MentoriaController(MentoriaService mentoriaService) {
        this.mentoriaService = mentoriaService;
    }

    @PostMapping
    public ResponseEntity<StandardApiResponse<SolicitacaoMentoriaDto.Response>> solicitar(
            @RequestBody @Valid SolicitacaoMentoriaDto.Request request,
            @RequestParam(required = false) Long usuarioId,
            Authentication authentication) {
        Long donoId = UsuarioDoToken.resolver(authentication, usuarioId);
        SolicitacaoMentoriaDto.Response response = mentoriaService.solicitar(request, donoId);
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }

    @GetMapping("/historico")
    public ResponseEntity<StandardApiResponse<List<SolicitacaoMentoriaDto.HistoricoResponse>>> historico(
            @RequestParam(required = false) Long usuarioId,
            Authentication authentication) {
        Long donoId = UsuarioDoToken.resolver(authentication, usuarioId);
        return ResponseEntity.ok(StandardApiResponse.ok(mentoriaService.buscarHistorico(donoId)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<StandardApiResponse<SolicitacaoMentoriaDto.Response>> atualizarStatus(
            @PathVariable Long id,
            @RequestBody @Valid SolicitacaoMentoriaDto.AtualizarStatusRequest request,
            Authentication authentication) {
        Long donoId = UsuarioDoToken.resolver(authentication, null);
        SolicitacaoMentoriaDto.Response response = mentoriaService.atualizarStatus(id, request.status(), donoId);
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }
}
