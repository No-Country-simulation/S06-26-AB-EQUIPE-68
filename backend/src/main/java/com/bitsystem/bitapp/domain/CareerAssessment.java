package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "career_assessments", indexes = {
    @Index(name = "idx_ca_user_id", columnList = "usuario_id"),
    @Index(name = "idx_ca_created_at", columnList = "created_at")
})
public class CareerAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    private Integer compatibilidade;

    private String nivel;

    @Lob
    @Column(name = "pontos_fortes")
    private String pontosFortes;

    @Lob
    private String gaps;

    @Lob
    @Column(name = "plano_desenvolvimento")
    private String planoDesenvolvimento;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CareerAssessment() {}

    public CareerAssessment(Long usuarioId, Integer compatibilidade, String nivel,
            String pontosFortes, String gaps, String planoDesenvolvimento) {
        this.usuarioId = usuarioId;
        this.compatibilidade = compatibilidade;
        this.nivel = nivel;
        this.pontosFortes = pontosFortes;
        this.gaps = gaps;
        this.planoDesenvolvimento = planoDesenvolvimento;
    }

    public static CareerAssessmentBuilder builder() {
        return new CareerAssessmentBuilder();
    }

    public static class CareerAssessmentBuilder {
        private Long usuarioId;
        private Integer compatibilidade;
        private String nivel;
        private String pontosFortes;
        private String gaps;
        private String planoDesenvolvimento;

        CareerAssessmentBuilder() {}

        public CareerAssessmentBuilder usuarioId(Long usuarioId) { this.usuarioId = usuarioId; return this; }
        public CareerAssessmentBuilder compatibilidade(Integer compatibilidade) { this.compatibilidade = compatibilidade; return this; }
        public CareerAssessmentBuilder nivel(String nivel) { this.nivel = nivel; return this; }
        public CareerAssessmentBuilder pontosFortes(String pontosFortes) { this.pontosFortes = pontosFortes; return this; }
        public CareerAssessmentBuilder gaps(String gaps) { this.gaps = gaps; return this; }
        public CareerAssessmentBuilder planoDesenvolvimento(String planoDesenvolvimento) { this.planoDesenvolvimento = planoDesenvolvimento; return this; }
        public CareerAssessment build() {
            return new CareerAssessment(usuarioId, compatibilidade, nivel, pontosFortes, gaps, planoDesenvolvimento);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Integer getCompatibilidade() { return compatibilidade; }
    public void setCompatibilidade(Integer compatibilidade) { this.compatibilidade = compatibilidade; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getPontosFortes() { return pontosFortes; }
    public void setPontosFortes(String pontosFortes) { this.pontosFortes = pontosFortes; }
    public String getGaps() { return gaps; }
    public void setGaps(String gaps) { this.gaps = gaps; }
    public String getPlanoDesenvolvimento() { return planoDesenvolvimento; }
    public void setPlanoDesenvolvimento(String planoDesenvolvimento) { this.planoDesenvolvimento = planoDesenvolvimento; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
