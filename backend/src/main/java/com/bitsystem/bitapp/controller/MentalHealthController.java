package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.MentalHealthDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.service.MentalHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MentalHealthController {

    private final MentalHealthService mentalHealthService;

    public MentalHealthController(MentalHealthService mentalHealthService) {
        this.mentalHealthService = mentalHealthService;
    }

    @PostMapping("/mental-health")
    public ResponseEntity<StandardApiResponse<MentalHealthDto.Response>> check(
            @RequestParam(defaultValue = "0") Long usuarioId) {
        MentalHealthDto.Response response = mentalHealthService.processar(usuarioId);
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }
}
