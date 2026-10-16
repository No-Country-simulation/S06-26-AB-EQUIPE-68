package com.bitsystem.bitapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
}
