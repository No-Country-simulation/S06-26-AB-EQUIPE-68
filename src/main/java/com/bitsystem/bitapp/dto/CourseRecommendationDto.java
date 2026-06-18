package com.bitsystem.bitapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseRecommendationDto {
    private String title;
    private String description;
    private String url;
    private String status;
    private String targetCareerLevel;
}
