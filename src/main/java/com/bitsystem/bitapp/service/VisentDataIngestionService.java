package com.bitsystem.bitapp.service;

import java.io.IOException;

/**
 * ============================================================================
 * SERVIÇO: VisentDataIngestionService
 * ============================================================================
 *
 * Interface para a rotina de ingestão de dados do Dataset Vísent (Anatel/Claro).
 * Lê arquivos CSV, cria os pontos geográficos espaciais e persiste no MySQL.
 *
 * @author BiT System
 * @version 1.0.0
 */
public interface VisentDataIngestionService {

    /**
     * Executa a carga de dados a partir dos arquivos CSV presentes em resources/data/.
     *
     * @param antenasCsvPath Caminho relativo ou absoluto do CSV de antenas (ex: "data/antenas_flp.csv")
     * @param concentracaoCsvPath Caminho do CSV de concentração (ex: "data/tensor_concentracao.csv")
     * @throws IOException Se houver erro na leitura dos arquivos
     */
    void importarDataset(String antenasCsvPath, String concentracaoCsvPath) throws IOException;
}
