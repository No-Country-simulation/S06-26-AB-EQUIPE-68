package com.bitsystem.bitapp.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    private Double latitude;

    private Double longitude;

    @Column(name = "idioma_preferido")
    private String idiomaPreferido = "pt";

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
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getIdiomaPreferido() { return idiomaPreferido; }
    public void setIdiomaPreferido(String idiomaPreferido) { this.idiomaPreferido = idiomaPreferido; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
