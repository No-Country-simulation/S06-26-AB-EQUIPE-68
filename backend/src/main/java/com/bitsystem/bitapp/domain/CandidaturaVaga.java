package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidatura_vaga", indexes = {
    @Index(name = "idx_candidatura_vaga_usuario", columnList = "usuario_id"),
    @Index(name = "idx_candidatura_vaga_vaga", columnList = "vaga_id")
})
public class CandidaturaVaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "vaga_id", nullable = false)
    private Long vagaId;

    private String nome;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "data_envio", nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    @Column(nullable = false)
    private String status;

    public CandidaturaVaga() {}

    public CandidaturaVaga(Long usuarioId, Long vagaId, String nome, String email,
            String mensagem, String status) {
        this.usuarioId = usuarioId;
        this.vagaId = vagaId;
        this.nome = nome;
        this.email = email;
        this.mensagem = mensagem;
        this.status = status;
    }

    public static CandidaturaVagaBuilder builder() {
        return new CandidaturaVagaBuilder();
    }

    public static class CandidaturaVagaBuilder {
        private Long usuarioId;
        private Long vagaId;
        private String nome;
        private String email;
        private String mensagem;
        private String status = "ENVIADA";

        CandidaturaVagaBuilder() {}

        public CandidaturaVagaBuilder usuarioId(Long usuarioId) { this.usuarioId = usuarioId; return this; }
        public CandidaturaVagaBuilder vagaId(Long vagaId) { this.vagaId = vagaId; return this; }
        public CandidaturaVagaBuilder nome(String nome) { this.nome = nome; return this; }
        public CandidaturaVagaBuilder email(String email) { this.email = email; return this; }
        public CandidaturaVagaBuilder mensagem(String mensagem) { this.mensagem = mensagem; return this; }
        public CandidaturaVagaBuilder status(String status) { this.status = status; return this; }
        public CandidaturaVaga build() {
            return new CandidaturaVaga(usuarioId, vagaId, nome, email, mensagem, status);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.dataEnvio = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ENVIADA";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getVagaId() { return vagaId; }
    public void setVagaId(Long vagaId) { this.vagaId = vagaId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}