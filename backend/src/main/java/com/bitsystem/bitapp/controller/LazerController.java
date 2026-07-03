package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.PontoLazerDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.service.LazerService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lazer")
public class LazerController {

    private final LazerService lazerService;

    public LazerController(LazerService lazerService) {
        this.lazerService = lazerService;
    }

    @GetMapping("/pontos")
    public ResponseEntity<StandardApiResponse<List<PontoLazerDto.Response>>> listarPontos() {
        return ResponseEntity.ok(StandardApiResponse.ok(lazerService.listarPontos()));
    }
}