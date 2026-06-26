package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mental_health_records", indexes = {
    @Index(name = "idx_mhr_user_id", columnList = "usuario_id"),
    @Index(name = "idx_mhr_created_at", columnList = "created_at")
})
public class MentalHealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    private String nivel;

    private String alerta;

    @Lob
    private String recomendacoes;

    @Lob
    private String acoes;

    @Lob
    @Column(name = "canais_apoio")
    private String canaisApoio;

    @Column(name = "derivar_cvv")
    private Boolean derivarCvv;

    @Column(name = "score_risco")
    private Integer scoreRisco;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MentalHealthRecord() {}

    public MentalHealthRecord(Long usuarioId, String nivel, String alerta, String recomendacoes,
            String acoes, String canaisApoio, Boolean derivarCvv, Integer scoreRisco) {
        this.usuarioId = usuarioId;
        this.nivel = nivel;
        this.alerta = alerta;
        this.recomendacoes = recomendacoes;
        this.acoes = acoes;
        this.canaisApoio = canaisApoio;
        this.derivarCvv = derivarCvv;
        this.scoreRisco = scoreRisco;
    }

    public static MentalHealthRecordBuilder builder() {
        return new MentalHealthRecordBuilder();
    }

    public static class MentalHealthRecordBuilder {
        private Long usuarioId;
        private String nivel;
        private String alerta;
        private String recomendacoes;
        private String acoes;
        private String canaisApoio;
        private Boolean derivarCvv;
        private Integer scoreRisco;

        MentalHealthRecordBuilder() {}

        public MentalHealthRecordBuilder usuarioId(Long usuarioId) { this.usuarioId = usuarioId; return this; }
        public MentalHealthRecordBuilder nivel(String nivel) { this.nivel = nivel; return this; }
        public MentalHealthRecordBuilder alerta(String alerta) { this.alerta = alerta; return this; }
        public MentalHealthRecordBuilder recomendacoes(String recomendacoes) { this.recomendacoes = recomendacoes; return this; }
        public MentalHealthRecordBuilder acoes(String acoes) { this.acoes = acoes; return this; }
        public MentalHealthRecordBuilder canaisApoio(String canaisApoio) { this.canaisApoio = canaisApoio; return this; }
        public MentalHealthRecordBuilder derivarCvv(Boolean derivarCvv) { this.derivarCvv = derivarCvv; return this; }
        public MentalHealthRecordBuilder scoreRisco(Integer scoreRisco) { this.scoreRisco = scoreRisco; return this; }
        public MentalHealthRecord build() {
            return new MentalHealthRecord(usuarioId, nivel, alerta, recomendacoes, acoes, canaisApoio,
                    derivarCvv, scoreRisco);
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
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getAlerta() { return alerta; }
    public void setAlerta(String alerta) { this.alerta = alerta; }
    public String getRecomendacoes() { return recomendacoes; }
    public void setRecomendacoes(String recomendacoes) { this.recomendacoes = recomendacoes; }
    public String getAcoes() { return acoes; }
    public void setAcoes(String acoes) { this.acoes = acoes; }
    public String getCanaisApoio() { return canaisApoio; }
    public void setCanaisApoio(String canaisApoio) { this.canaisApoio = canaisApoio; }
    public Boolean getDerivarCvv() { return derivarCvv; }
    public void setDerivarCvv(Boolean derivarCvv) { this.derivarCvv = derivarCvv; }
    public Integer getScoreRisco() { return scoreRisco; }
    public void setScoreRisco(Integer scoreRisco) { this.scoreRisco = scoreRisco; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
