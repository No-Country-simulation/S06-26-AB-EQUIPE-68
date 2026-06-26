package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String instituicao;

    @Column(nullable = false)
    private String regiao;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private String area;

    private String nivel;

    private String duracao;

    private String modalidade;

    private boolean gratuito;

    private boolean certificado;

    private String vagas;

    private String link;

    private String beneficente;

    private boolean ativa = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Curso() {}

    public Curso(String titulo, String instituicao, String regiao, String descricao,
                 String area, String nivel, String duracao, String modalidade,
                 boolean gratuito, boolean certificado, String vagas, String link,
                 String beneficente) {
        this.titulo = titulo;
        this.instituicao = instituicao;
        this.regiao = regiao;
        this.descricao = descricao;
        this.area = area;
        this.nivel = nivel;
        this.duracao = duracao;
        this.modalidade = modalidade;
        this.gratuito = gratuito;
        this.certificado = certificado;
        this.vagas = vagas;
        this.link = link;
        this.beneficente = beneficente;
        this.ativa = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getInstituicao() { return instituicao; }
    public void setInstituicao(String instituicao) { this.instituicao = instituicao; }
    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getDuracao() { return duracao; }
    public void setDuracao(String duracao) { this.duracao = duracao; }
    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }
    public boolean isGratuito() { return gratuito; }
    public void setGratuito(boolean gratuito) { this.gratuito = gratuito; }
    public boolean isCertificado() { return certificado; }
    public void setCertificado(boolean certificado) { this.certificado = certificado; }
    public String getVagas() { return vagas; }
    public void setVagas(String vagas) { this.vagas = vagas; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getBeneficente() { return beneficente; }
    public void setBeneficente(String beneficente) { this.beneficente = beneficente; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
