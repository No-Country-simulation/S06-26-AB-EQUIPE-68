package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.service.RoadmapService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping("/{area}")
    public ResponseEntity<StandardApiResponse<JsonNode>> buscarPorArea(@PathVariable String area) {
        JsonNode roadmap = roadmapService.buscarPorArea(area);
        return ResponseEntity.ok(StandardApiResponse.ok(roadmap));
    }
}
