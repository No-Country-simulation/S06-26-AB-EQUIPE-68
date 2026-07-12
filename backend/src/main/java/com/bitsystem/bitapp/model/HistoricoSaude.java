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

    // Opcional (CVV v2): check-in sem emoji (só texto) grava nota=null —
    // nunca gera gatilho nem entra na agregação/tendência. Nota fixa por
    // nível (ver NivelCheckin), nunca inferida do texto/IA.
    @Column
    private Integer nota;

    @Column(columnDefinition = "TEXT")
    private String contexto;

    @Column(name = "derivou_cvv")
    private Boolean derivouCvv;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        // Só preenche se ausente: permite testes fixarem createdAt para
        // simular dias diferentes (agregação diária / tendência semanal).
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
