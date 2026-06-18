package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.model.HistoricoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ============================================================================
 * REPOSITORY: HistoricoSaudeRepository
 * ============================================================================
 * 
 * Data Access Layer para entidade HistoricoSaude.
 * Fornece operações CRUD + queries para análise de histórico.
 * 
 * RESPONSABILIDADES:
 * - CRUD automático: save(), findById(), findAll(), delete()
 * - Query customizada: findByUsuarioIdOrderByDataRegistroDesc()
 * - Suporte a análise de tendências (histórico temporal)
 * 
 * HERANÇA:
 * - JpaRepository<HistoricoSaude, Long>:
 *   ├─ Entity: HistoricoSaude
 *   └─ PK type: Long (id)
 * 
 * MÉTODOS HERDADOS (automáticos):
 * - save(saude): INSERT em tb_historico_saude
 * - findById(id): SELECT by PK
 * - findAll(): SELECT * from tb_historico_saude
 * - delete(saude): DELETE by PK
 * - count(): SELECT COUNT(*) from tb_historico_saude
 * 
 * @author BiT System
 * @version 1.0.0
 */
@Repository
public interface HistoricoSaudeRepository extends JpaRepository<HistoricoSaude, Long> {
    
    /**
     * Query customizada: histórico de saúde de um usuário
     * 
     * Spring Data JPA interpreta o método signature:
     * - findBy + UsuarioId: SELECT ... WHERE usuario_id = ?
     * - OrderBy + DataRegistro + Desc: ORDER BY data_registro DESC (mais recente primeiro)
     * 
     * RETORNA:
     * - List<HistoricoSaude>: registros ordenados (mais recentes primeiro)
     * 
     * SQL GERADO:
     * SELECT * FROM tb_historico_saude 
     * WHERE usuario_id = ? 
     * ORDER BY data_registro DESC
     * 
     * CASOS DE USO:
     * - Dashboard: exibir últimos check-ins do usuário
     * - Análise de tendências: detectar deterioração de saúde
     * - Relatórios: gerar summário temporal de bem-estar
     * - Alertas: verificar se há padrão de notas baixas
     * 
     * EXEMPLO:
     * List<HistoricoSaude> historico = repo.findByUsuarioIdOrderByDataRegistroDesc(userId);
     * // Retorna: [check-in de hoje, check-in de ontem, check-in de 2 dias atrás, ...]
     * 
     * @param usuarioId ID do usuário (FK em HistoricoSaude)
     * @return List de registros ordenados por data DESC (mais recentes primeiro)
     */
    List<HistoricoSaude> findByUsuarioIdOrderByDataRegistroDesc(Long usuarioId);
}
