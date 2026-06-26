package com.bitsystem.bitapp.dto;

import com.bitsystem.bitapp.domain.Vaga;
import java.time.LocalDateTime;

public record VagaDto(
    Long id,
    String titulo,
    String empresa,
    String regiao,
    String descricao,
    String nivel,
    String area,
    String tipoContrato,
    String salario,
    String tecnologias,
    String link,
    boolean ativa,
    String remoto,
    LocalDateTime createdAt
) {
    public static VagaDto from(Vaga v) {
        return new VagaDto(
            v.getId(),
            v.getTitulo(),
            v.getEmpresa(),
            v.getRegiao(),
            v.getDescricao(),
            v.getNivel(),
            v.getArea(),
            v.getTipoContrato(),
            v.getSalario(),
            v.getTecnologias(),
            v.getLink(),
            v.isAtiva(),
            v.getRemoto(),
            v.getCreatedAt()
        );
    }
}
