package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_mentoria", indexes = {
    @Index(name = "idx_solic_mentoria_usuario", columnList = "usuario_id"),
    @Index(name = "idx_solic_mentoria_mentor", columnList = "mentor_id")
})
public class SolicitacaoMentoria {

    public static final String AGUARDANDO_CONFIRMACAO = "AGUARDANDO_CONFIRMACAO";
    public static final String CONFIRMADA = "CONFIRMADA";
    public static final String CONCLUIDA = "CONCLUIDA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    @Column(name = "area_solicitada", nullable = false)
    private String areaSolicitada;

    // Sem @Lob: em Postgres, @Lob numa String vira Large Object (oid), que só
    // pode ser lido dentro da MESMA transação em que foi gravado. Ver
    // CareerAssessment/HistoricoSaude para o mesmo padrão.
    @Column(columnDefinition = "TEXT")
    private String mensagem;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SolicitacaoMentoria() {}

    public SolicitacaoMentoria(Long usuarioId, Long mentorId, String areaSolicitada,
            String mensagem, String status) {
        this.usuarioId = usuarioId;
        this.mentorId = mentorId;
        this.areaSolicitada = areaSolicitada;
        this.mensagem = mensagem;
        this.status = status;
    }

    public static SolicitacaoMentoriaBuilder builder() {
        return new SolicitacaoMentoriaBuilder();
    }

    public static class SolicitacaoMentoriaBuilder {
        private Long usuarioId;
        private Long mentorId;
        private String areaSolicitada;
        private String mensagem;
        private String status = AGUARDANDO_CONFIRMACAO;

        SolicitacaoMentoriaBuilder() {}

        public SolicitacaoMentoriaBuilder usuarioId(Long usuarioId) { this.usuarioId = usuarioId; return this; }
        public SolicitacaoMentoriaBuilder mentorId(Long mentorId) { this.mentorId = mentorId; return this; }
        public SolicitacaoMentoriaBuilder areaSolicitada(String areaSolicitada) { this.areaSolicitada = areaSolicitada; return this; }
        public SolicitacaoMentoriaBuilder mensagem(String mensagem) { this.mensagem = mensagem; return this; }
        public SolicitacaoMentoriaBuilder status(String status) { this.status = status; return this; }
        public SolicitacaoMentoria build() {
            return new SolicitacaoMentoria(usuarioId, mentorId, areaSolicitada, mensagem, status);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AGUARDANDO_CONFIRMACAO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getMentorId() { return mentorId; }
    public void setMentorId(Long mentorId) { this.mentorId = mentorId; }
    public String getAreaSolicitada() { return areaSolicitada; }
    public void setAreaSolicitada(String areaSolicitada) { this.areaSolicitada = areaSolicitada; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
