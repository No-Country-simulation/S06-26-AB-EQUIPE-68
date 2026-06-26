package com.bitsystem.bitapp.dto;

import com.bitsystem.bitapp.domain.Curso;
import java.time.LocalDateTime;

public record CursoDto(
    Long id,
    String titulo,
    String instituicao,
    String regiao,
    String descricao,
    String area,
    String nivel,
    String duracao,
    String modalidade,
    boolean gratuito,
    boolean certificado,
    String vagas,
    String link,
    String beneficente,
    boolean ativa,
    LocalDateTime createdAt
) {
    public static CursoDto from(Curso c) {
        return new CursoDto(
            c.getId(),
            c.getTitulo(),
            c.getInstituicao(),
            c.getRegiao(),
            c.getDescricao(),
            c.getArea(),
            c.getNivel(),
            c.getDuracao(),
            c.getModalidade(),
            c.isGratuito(),
            c.isCertificado(),
            c.getVagas(),
            c.getLink(),
            c.getBeneficente(),
            c.isAtiva(),
            c.getCreatedAt()
        );
    }
}
