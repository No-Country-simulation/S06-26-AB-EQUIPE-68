package com.bitsystem.bitapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Configuração de segurança que não é regra de rota: console H2, cabeçalhos e CORS. */
@SpringBootTest
@AutoConfigureMockMvc
class ConfiguracaoSegurancaTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void consoleH2_naoAbreSemToken() throws Exception {
        MvcResult res = mvc.perform(get("/h2-console")).andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void consoleH2_naoAbreNemEmSubcaminhos() throws Exception {
        assertThat(mvc.perform(get("/h2-console/")).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(mvc.perform(get("/h2-console/login.jsp")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void respostasNaoPodemSerEmbutidasEmIframe() throws Exception {
        MvcResult res = mvc.perform(get("/manifest.json")).andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getHeader("X-Frame-Options")).isEqualTo("DENY");
    }

    // ── CORS: uma só configuração, origens vindas de APP_CORS_ALLOWED_ORIGINS ──

    @Test
    void cors_origemPermitida_recebeOsCabecalhos() throws Exception {
        MvcResult res = mvc.perform(options("/api/saude")
                .header("Origin", "http://localhost:8080")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type")).andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getHeader("Access-Control-Allow-Origin")).isEqualTo("http://localhost:8080");
        assertThat(res.getResponse().getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
    }

    @Test
    void cors_origemDesconhecida_ehRecusada() throws Exception {
        MvcResult res = mvc.perform(options("/api/saude")
                .header("Origin", "https://site-malicioso.example")
                .header("Access-Control-Request-Method", "POST")).andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(403);
        assertThat(res.getResponse().getHeader("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    void cors_requisicaoRealDeOrigemDesconhecida_naoRecebeAllowOrigin() throws Exception {
        MvcResult res = mvc.perform(get("/api/vagas").header("Origin", "https://site-malicioso.example")).andReturn();

        assertThat(res.getResponse().getHeader("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    void cors_listaDeOrigens_aceitaVariasESeparaPorVirgula() {
        assertThat(SecurityConfig.parseOrigens(" http://localhost:8080 , https://app.exemplo.com "))
                .containsExactly("http://localhost:8080", "https://app.exemplo.com");
    }

    @Test
    void cors_curingaNaoEhAceito() {
        assertThatThrownBy(() -> SecurityConfig.parseOrigens("*"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("curinga");
        assertThatThrownBy(() -> SecurityConfig.parseOrigens("https://*.exemplo.com"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("curinga");
    }

    @Test
    void cors_listaVaziaOuOrigemSemEsquemaNaoSobe() {
        assertThatThrownBy(() -> SecurityConfig.parseOrigens(" , "))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("vazio");
        assertThatThrownBy(() -> SecurityConfig.parseOrigens("app.exemplo.com"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("inválida");
    }
}
