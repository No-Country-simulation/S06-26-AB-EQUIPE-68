package com.bitsystem.bitapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.*;
import org.locationtech.jts.geom.Point;

/**
 * ============================================================================
 * ENTIDADE JPA: Usuario
 * ============================================================================
 *
 * Representa um usuário do ecossistema BiT com dados pessoais, profissionais
 * e geolocalização. Persisted em tb_usuarios no MySQL.
 *
 * RESPONSABILIDADES:
 * - Armazenar perfil completo do usuário (onboarding)
 * - Correlacionar histórico de saúde mental e orientações
 * - Rastrear localização geográfica para recomendações regionalizadas
 * - Fornecer contexto para IA Gemini em análises de orientação
 *
 * SEÇÕES DE DADOS:
 * ┌─ PESSOAIS: nome, email, data nascimento, gênero, localização
 * ├─ PROFISSIONAIS: nível, área tecnológica, objetivos, competências
 * └─ GEOREFERENCIAMENTO: latitude/longitude (integração CDRView)
 *
 * RESTRIÇÕES:
 * - Email único por usuário
 * - Nome e Email obrigatórios
 * - Gerenciado por Lombok (getters, setters, constructors, builders)
 *
 * RELACIONAMENTOS:
 * - Monodirecional com HistoricoSaude (1:N via usuario_id)
 *
 * @author BiT System
 * @version 1.0.0
 */
@Entity
@Table(name = "tb_usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    // ========== CHAVE PRIMÁRIA ==========
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== DADOS PESSOAIS ==========
    /** Nome completo obrigatório do usuário */
    @NotBlank
    @Column(nullable = false)
    private String nome;

    /** Email único para login/contato; validado via Jakarta validation */
    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    /** Data de nascimento para análise demográfica */
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    /** Gênero (ex: Masculino, Feminino, Não Binário, Prefiro Não Informar) */
    private String genero;

    /** Município para análise local de mercado */
    private String cidade;

    /** WhatsApp para notificações e suporte */
    private String whatsapp;

    // ========== DADOS PROFISSIONAIS ==========
    /**
     * Nível profissional na tecnologia
     * Exemplos: "Iniciante", "Transição", "Graduado sem Experiência", "Mid-level", "Senior"
     * Usado para recomendar trilhas personalizadas
     */
    private String nivelProfissional;

    /**
     * Área de tecnologia de interesse
     * Exemplos: "Java Back-End", "Frontend React", "Análise de Dados", "DevOps", "QA"
     */
    private String areaTecnologia;

    /**
     * Objetivo principal do usuário na plataforma
     * Exemplos: "Estudar", "Buscar Emprego", "Mudar de Emprego", "Networking"
     */
    private String objetivoPrincipal;

    /**
     * Competências atuais separadas por vírgula (MVP simplificado)
     * Exemplo: "Java, MySQL, Git, REST APIs, Spring Boot"
     * Usado para calcular gaps e sugerir trilhas de estudo
     */
    @Column(columnDefinition = "TEXT")
    private String competenciasAtuais;

    // ========== GEOREFERENCIAMENTO ==========
    /** Ponto geográfico do usuário (latitude e longitude) para buscas espaciais */
    private Point localizacao;
}
