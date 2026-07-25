package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentores")
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String papel;

    // CSV de valores do dropdown "Área Alvo" (ex.: "Java,DevOps,IA") — mesmo
    // espírito de User.competenciasAtuais, evita tabela de junção para 4 registros fixos.
    @Column(name = "areas_atendidas", nullable = false)
    private String areasAtendidas;

    @Column(name = "link_sala", nullable = false)
    private String linkSala;

    private boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Mentor() {}

    public Mentor(String nome, String papel, String areasAtendidas, String linkSala) {
        this.nome = nome;
        this.papel = papel;
        this.areasAtendidas = areasAtendidas;
        this.linkSala = linkSala;
        this.ativo = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }
    public String getAreasAtendidas() { return areasAtendidas; }
    public void setAreasAtendidas(String areasAtendidas) { this.areasAtendidas = areasAtendidas; }
    public String getLinkSala() { return linkSala; }
    public void setLinkSala(String linkSala) { this.linkSala = linkSala; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
