package com.bitsystem.bitapp.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * DTO para representar o status da rede e a tecnologia predominante.
 */
@Data
@Builder
public class NetworkStatusDto {

    private String status;
    private String tecnologiaPredominante;
    private String cssClass; // Para o frontend (ex: 'text-yellow-500' ou 'text-blue-500')
    private boolean sugerirOffline;
    private String recomendacao;
    private List<CourseRecommendationDto> courseRecommendations;
}
