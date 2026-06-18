package com.bitsystem.bitapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * ============================================================================
 * ENTIDADE JPA: HistoricoSaude
 * ============================================================================
 *
 * Registra snapshots do estado mental e bem-estar do usuário ao longo do tempo.
 * Persisted em tb_historico_saude no MySQL.
 *
 * RESPONSABILIDADES:
 * - Auditar check-ins de saúde mental (humor, nota de bem-estar)
 * - Rastrear padrões de comportamento e criando alertas críticos
 * - Fornecer dados históricos para análise por IA Gemini
 * - Derivar sinalizações de risco para equipes de suporte
 *
 * FLUXO DE DADOS:
 * 1. Usuário preenche check-in via /api/saude
 * 2. SaudeMentalService processa e armazena HistoricoSaude
 * 3. Se nota < 4, derivouCvv = true (alerta de Centro de Valorização da Vida)
 * 4. Dashboard recupera histórico para exibição de tendências
 *
 * RESTRIÇÕES:
 * - Relacionamento com Usuario obrigatório (FK não nula)
 * - Lazy loading para otimização (não carrega Usuário automaticamente)
 * - Timestamp preenchido automaticamente no momento da criação
 *
 * @author BiT System
 * @version 1.0.0
 */
@Entity
@Table(name = "tb_historico_saude")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoSaude {

    // ========== CHAVE PRIMÁRIA ==========
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== RELACIONAMENTO ==========
    /**
     * Referência bidirecional com Usuario (FK para tb_usuarios)
     * LAZY loading evita carregar Usuario desnecessariamente em queries
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // ========== DADOS DE CHECK-IN ==========
    /**
     * Representação textual/emoji do estado de humor
     * Exemplos: "😢 Triste", "😰 Ansioso", "😊 Feliz", "😴 Cansado"
     * Obrigatório para todo check-in
     */
    @Column(nullable = false)
    private String humor;

    /**
     * Escala numérica de bem-estar semanal (1 a 5)
     * 1 = Muito ruim (risco crítico → derivouCvv = true)
     * 2-3 = Ruim ou regular
     * 4-5 = Bom ou excelente
     * Utilizado para detecção automática de alertas
     */
    @Column(nullable = false)
    private Integer notaSemanal;

    /**
     * Contexto livre do usuário sobre seu estado
     * Exemplos: "Muitas pressões no trabalho", "Problema familiar"
     * Enviado ao Gemini para recomendações empáticas específicas
     */
    @Column(columnDefinition = "TEXT")
    private String contexto;

    /**
     * Timestamp automático de quando o check-in foi registrado
     * Preenchido via LocalDateTime.now() no builder
     */
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime dataRegistro = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.dataRegistro == null) {
            this.dataRegistro = LocalDateTime.now();
        }
    }

    /**
     * Flag que indica se houve derivação para Centro de Valorização da Vida
     * Ativada automaticamente quando notaSemanal < 4
     * Sinaliza para equipe de suporte/acolhimento da plataforma
     */
    private Boolean derivouCvv;
}
