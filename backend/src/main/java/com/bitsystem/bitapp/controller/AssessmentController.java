package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.AssessmentDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(defaultValue = "0") Long usuarioId) {
        AssessmentDto.Response response = assessmentService.processar(request, usuarioId);
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }
}
