package com.bitsystem.bitapp.dto;

import java.util.List;

public class MentalHealthDto {

    public record Response(
        String nivel,
        String alerta,
        List<String> recomendacoes,
        List<String> acoes,
        List<String> canaisApoio,
        Boolean derivarCvv,
        Integer scoreRisco
    ) {}
}
