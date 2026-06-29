-- ============================================================
-- BiT App — Script de Limpeza do Banco de Dados
-- Data: 2026-06-29
-- Objetivo: Remover tabelas nao utilizadas e corrigir schema
-- ============================================================

-- 1. Remover tabela skill_catalog (nenhum codigo a referencia)
DROP TABLE IF EXISTS skill_catalog;

-- 2. Corrigir historico_saude: remover coluna user_id antiga (se existir)
--    e garantir que usuario_id existe e e NOT NULL
SET @exists_user_id = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'historico_saude'
      AND COLUMN_NAME = 'user_id'
);

SET @sql_user_id = IF(@exists_user_id > 0,
    'ALTER TABLE historico_saude DROP COLUMN user_id',
    'SELECT "coluna user_id nao existe, nada a fazer"'
);
PREPARE stmt FROM @sql_user_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Garantir que usuario_id e NOT NULL na historico_saude
SET @exists_usuario_id = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'historico_saude'
      AND COLUMN_NAME = 'usuario_id'
);

SET @sql_not_null = IF(@exists_usuario_id > 0,
    'ALTER TABLE historico_saude MODIFY COLUMN usuario_id BIGINT NOT NULL',
    'SELECT "coluna usuario_id nao existe, criando..."'
);
PREPARE stmt2 FROM @sql_not_null;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 4. (Opcional) Remover mental_health_records se nao for mais usado pelo N8N
-- DESCOMENTE AS LINHAS ABAIXO SE CONFIRMAR QUE O ENDPOINT /api/mental-health NAO E MAIS UTILIZADO:
-- DROP TABLE IF EXISTS mental_health_records;

-- 5. Verificar tabelas restantes
SELECT TABLE_NAME, TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
