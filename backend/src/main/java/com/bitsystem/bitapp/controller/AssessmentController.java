package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.config.UsuarioDoToken;
import com.bitsystem.bitapp.dto.AssessmentDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/assessment")
    public ResponseEntity<StandardApiResponse<AssessmentDto.Response>> assess(
            @RequestBody @Valid AssessmentDto.Request request,
            @RequestParam(required = false) Long usuarioId,
            Authentication authentication) {
        Long donoId = UsuarioDoToken.resolver(authentication, usuarioId);
        AssessmentDto.Response response = assessmentService.processar(request, donoId);
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }

    @GetMapping("/assessment/historico")
    public ResponseEntity<StandardApiResponse<java.util.List<AssessmentDto.HistoricoResponse>>> historico(
            @RequestParam(required = false) Long usuarioId,
            Authentication authentication) {
        Long donoId = UsuarioDoToken.resolver(authentication, usuarioId);
        return ResponseEntity.ok(StandardApiResponse.ok(assessmentService.buscarHistorico(donoId)));
    }
}
