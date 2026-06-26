package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vagas")
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String empresa;

    @Column(nullable = false)
    private String regiao;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private String nivel;

    @Column(nullable = false)
    private String area;

    private String tipoContrato;

    private String salario;

    @Column(columnDefinition = "TEXT")
    private String tecnologias;

    private String link;

    private boolean ativa = true;

    private String remoto;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Vaga() {}

    public Vaga(String titulo, String empresa, String regiao, String descricao,
                String nivel, String area, String tipoContrato, String salario,
                String tecnologias, String link, String remoto) {
        this.titulo = titulo;
        this.empresa = empresa;
        this.regiao = regiao;
        this.descricao = descricao;
        this.nivel = nivel;
        this.area = area;
        this.tipoContrato = tipoContrato;
        this.salario = salario;
        this.tecnologias = tecnologias;
        this.link = link;
        this.remoto = remoto;
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
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(String tipoContrato) { this.tipoContrato = tipoContrato; }
    public String getSalario() { return salario; }
    public void setSalario(String salario) { this.salario = salario; }
    public String getTecnologias() { return tecnologias; }
    public void setTecnologias(String tecnologias) { this.tecnologias = tecnologias; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public String getRemoto() { return remoto; }
    public void setRemoto(String remoto) { this.remoto = remoto; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
