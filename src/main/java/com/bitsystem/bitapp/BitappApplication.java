package com.bitsystem.bitapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * CLASSE PRINCIPAL: BitappApplication
 * ============================================================================
 * 
 * Ponto de entrada da aplicação Spring Boot do App BiT.
 * 
 * RESPONSABILIDADES:
 * - Inicializar e executar a aplicação Spring Boot
 * - Configurar automaticamente componentes, beans e contexto da aplicação
 * - Habilitar autodiscovery de componentes no pacote com.bitsystem.bitapp
 * 
 * FLUXO EXECUTADO:
 * 1. JVM localiza a classe com @SpringBootApplication
 * 2. Spring Boot escaneia o pacote com.bitsystem.bitapp e subpacotes
 * 3. Carrega application.properties e variáveis de ambiente
 * 4. Inicializa DataSource (MySQL), JPA/Hibernate
 * 5. Detecta Controllers (@RestController, @Controller) e Services (@Service)
 * 6. Inicia servidor Tomcat na porta 8080 (padrão)
 * 
 * TECNOLOGIAS UTILIZADAS:
 * - Spring Boot 3.3.4
 * - Java 21
 * - Spring Data JPA + Hibernate
 * - MySQL 8.0+
 * 
 * @author BiT System
 * @version 1.0.0-SNAPSHOT
 */
@SpringBootApplication
public class BitappApplication {

    public static void main(String[] args) {
        SpringApplication.run(BitappApplication.class, args);
    }
}
