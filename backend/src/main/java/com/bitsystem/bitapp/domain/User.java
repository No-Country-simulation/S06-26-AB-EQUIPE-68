package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String cidade;

    private String whatsapp;

    @Column(name = "nivel_profissional")
    private String nivelProfissional;

    @Column(name = "area_tecnologia")
    private String areaTecnologia;

    @Column(name = "competencias_atuais", columnDefinition = "TEXT")
    private String competenciasAtuais;

    @Column(columnDefinition = "GEOMETRY")
    private Point localizacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User() {}

    public User(String nome, String email, String passwordHash) {
        this.nome = nome;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getNivelProfissional() { return nivelProfissional; }
    public void setNivelProfissional(String nivelProfissional) { this.nivelProfissional = nivelProfissional; }
    public String getAreaTecnologia() { return areaTecnologia; }
    public void setAreaTecnologia(String areaTecnologia) { this.areaTecnologia = areaTecnologia; }
    public String getCompetenciasAtuais() { return competenciasAtuais; }
    public void setCompetenciasAtuais(String competenciasAtuais) { this.competenciasAtuais = competenciasAtuais; }
    public Point getLocalizacao() { return localizacao; }
    public void setLocalizacao(Point localizacao) { this.localizacao = localizacao; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
