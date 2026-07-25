package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.MentorDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.service.MentoriaService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentores")
public class MentorController {

    private final MentoriaService mentoriaService;

    public MentorController(MentoriaService mentoriaService) {
        this.mentoriaService = mentoriaService;
    }

    @GetMapping
    public ResponseEntity<StandardApiResponse<List<MentorDto>>> listar() {
        return ResponseEntity.ok(StandardApiResponse.ok(mentoriaService.listarMentores()));
    }
}
