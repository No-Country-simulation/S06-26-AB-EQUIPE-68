package com.bitsystem.bitapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_saude")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Opcional (lote 4.1): check-in diário pode chegar só com texto, sem emoji.
    @Column
    private String humor;

    // Opcional (lote 4.1): ausente no check-in diário; presente só quando a
    // pergunta semanal é respondida (nota fixa por emoji, ver SEMANA_NOTA).
    @Column(name = "nota_semanal")
    private Integer notaSemanal;

    @Column(columnDefinition = "TEXT")
    private String contexto;

    @Column(name = "derivou_cvv")
    private Boolean derivouCvv;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
