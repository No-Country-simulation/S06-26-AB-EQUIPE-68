package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscricao_curso", indexes = {
    @Index(name = "idx_inscricao_curso_usuario", columnList = "usuario_id"),
    @Index(name = "idx_inscricao_curso_curso", columnList = "curso_id")
})
public class InscricaoCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    @Column(name = "data_inscricao", nullable = false, updatable = false)
    private LocalDateTime dataInscricao;

    @Column(nullable = false)
    private String status;

    public InscricaoCurso() {}

    public InscricaoCurso(Long usuarioId, Long cursoId, String status) {
        this.usuarioId = usuarioId;
        this.cursoId = cursoId;
        this.status = status;
    }

    public static InscricaoCursoBuilder builder() {
        return new InscricaoCursoBuilder();
    }

    public static class InscricaoCursoBuilder {
        private Long usuarioId;
        private Long cursoId;
        private String status = "INSCRITO";

        InscricaoCursoBuilder() {}

        public InscricaoCursoBuilder usuarioId(Long usuarioId) { this.usuarioId = usuarioId; return this; }
        public InscricaoCursoBuilder cursoId(Long cursoId) { this.cursoId = cursoId; return this; }
        public InscricaoCursoBuilder status(String status) { this.status = status; return this; }
        public InscricaoCurso build() {
            return new InscricaoCurso(usuarioId, cursoId, status);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.dataInscricao = LocalDateTime.now();
        if (this.status == null) {
            this.status = "INSCRITO";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getCursoId() { return cursoId; }
    public void setCursoId(Long cursoId) { this.cursoId = cursoId; }
    public LocalDateTime getDataInscricao() { return dataInscricao; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}