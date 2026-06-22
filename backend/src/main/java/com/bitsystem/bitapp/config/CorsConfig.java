package com.bitsystem.bitapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ============================================================================
 * CONFIGURAÇÃO GLOBAL DE CORS
 * ============================================================================
 *
 * Habilita o frontend estático (servido em outro host/porta) a fazer chamadas
 * à API REST do BiT App sem ser bloqueado pelo browser por política de
 * mesma origem (Same-Origin Policy).
 *
 * Em desenvolvimento: aceita qualquer origem (*)
 * Em produção: substituir allowedOrigins por domínio específico do frontend
 *
 * @author BiT System
 * @version 1.0.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
