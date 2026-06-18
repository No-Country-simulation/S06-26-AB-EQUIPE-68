package com.bitsystem.bitapp.config;

import com.bitsystem.bitapp.service.VisentDataIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 * CONFIGURAÇÃO: VisentDataInitializer
 * ============================================================================
 *
 * Dispara automaticamente a rotina de ingestão do Dataset Vísent no momento de
 * inicialização do Spring Boot. Garante que os dados geográficos e de tráfego
 * estejam populados no MySQL para o funcionamento correto do ecossistema.
 *
 * @author BiT System
 * @version 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "bitapp.visent.enabled", havingValue = "true")
public class VisentDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(
        VisentDataInitializer.class
    );

    private final VisentDataIngestionService ingestionService;

    public VisentDataInitializer(VisentDataIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void run(String... args) {
        try {
            log.info(
                "VisentDataInitializer: Iniciando verificação de carga inicial..."
            );

            // Define os caminhos relativos ao classpath do projeto
            String antenasPath = "data/antenas_flp.csv";
            String concentracaoPath = "data/tensor_concentracao.csv";

            ingestionService.importarDataset(antenasPath, concentracaoPath);
            log.info(
                "VisentDataInitializer: Carga inicial de dados concluída com sucesso."
            );
        } catch (Exception e) {
            log.error(
                "VisentDataInitializer: Erro ao executar a carga de dados na inicialização: ",
                e
            );
        }
    }
}
