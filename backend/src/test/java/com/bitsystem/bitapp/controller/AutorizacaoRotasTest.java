package com.bitsystem.bitapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Matriz de autorização das rotas com dados de usuário (MockMvc, com a cadeia de
 * segurança real e o filtro JWT): sem token devolve 401, token de outro usuário
 * devolve 403 e o dono devolve 200. O usuário vem sempre do token; o usuarioId
 * enviado pelo cliente só é aceito quando é o do dono.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutorizacaoRotasTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private Conta dono;
    private Conta outro;
    private Long vagaId;
    private Long cursoId;
    private Long mentorId;

    record Conta(Long id, String token) {}

    /** Uma rota protegida; {@code caminho} e {@code corpo} recebem o usuarioId que o cliente envia. */
    record Rota(String nome, HttpMethod metodo, Function<Long, String> caminho, Function<Long, String> corpo) {
        @Override
        public String toString() {
            return nome;
        }
    }

    @BeforeAll
    void prepararContasEIdsDoCatalogo() throws Exception {
        dono = registrar("dono");
        outro = registrar("outro");
        vagaId = primeiroIdDe("/api/vagas");
        cursoId = primeiroIdDe("/api/cursos");
        mentorId = primeiroIdDe("/api/mentores");
    }

    // ── matriz: 401 / 403 / 200 ─────────────────────────────────────────

    Stream<Rota> rotasProtegidas() {
        return Stream.of(
            new Rota("POST /api/orientar", HttpMethod.POST, id -> "/api/orientar",
                id -> corpo("usuarioId", id, "perfil", "Java, Spring", "nivel", "Junior", "regiao", "TRINDADE", "idioma", "pt")),
            new Rota("POST /api/saude", HttpMethod.POST, id -> "/api/saude",
                id -> corpo("usuarioId", id, "nota", 5, "contexto", "Dia tranquilo", "idioma", "pt")),
            new Rota("GET /api/saude/historico", HttpMethod.GET, id -> "/api/saude/historico?usuarioId=" + id, id -> null),
            new Rota("GET /api/network-status/{usuarioId}", HttpMethod.GET, id -> "/api/network-status/" + id, id -> null),
            new Rota("POST /api/assessment", HttpMethod.POST, id -> "/api/assessment?usuarioId=" + id,
                id -> corpo("nome", "Teste", "hardSkills", List.of("Java"), "area", "Java")),
            new Rota("GET /api/assessment/historico", HttpMethod.GET, id -> "/api/assessment/historico?usuarioId=" + id, id -> null),
            new Rota("GET /api/vagas/{id}/match", HttpMethod.GET, id -> "/api/vagas/" + vagaId + "/match?usuarioId=" + id, id -> null),
            new Rota("GET /api/vagas/match-lote", HttpMethod.GET, id -> "/api/vagas/match-lote?usuarioId=" + id, id -> null),
            new Rota("POST /api/cursos/inscrever", HttpMethod.POST, id -> "/api/cursos/inscrever",
                id -> corpo("cursoId", cursoId, "usuarioId", id)),
            new Rota("POST /api/vagas/enviar-curriculo", HttpMethod.POST, id -> "/api/vagas/enviar-curriculo",
                id -> corpo("vagaId", vagaId, "usuarioId", id)),
            new Rota("POST /api/mentorias", HttpMethod.POST, id -> "/api/mentorias?usuarioId=" + id,
                id -> corpo("mentorId", mentorId, "areaSolicitada", "Java", "mensagem", "Quero orientação")),
            new Rota("GET /api/mentorias/historico", HttpMethod.GET, id -> "/api/mentorias/historico?usuarioId=" + id, id -> null),
            new Rota("GET /api/sugestoes/{usuarioId}", HttpMethod.GET, id -> "/api/sugestoes/" + id, id -> null),
            new Rota("PUT /api/usuarios/{id}/localizacao", HttpMethod.PUT, id -> "/api/usuarios/" + id + "/localizacao",
                id -> corpo("latitude", -27.5954, "longitude", -48.5480))
        );
    }

    @ParameterizedTest(name = "{0}: sem token devolve 401")
    @MethodSource("rotasProtegidas")
    void semToken_devolve401(Rota rota) throws Exception {
        MvcResult res = chamar(rota, dono.id(), null);

        assertThat(res.getResponse().getStatus()).isEqualTo(401);
        assertThat(corpoJson(res).path("codigo").asText()).isEqualTo("NAO_AUTENTICADO");
    }

    @ParameterizedTest(name = "{0}: token de outro usuário devolve 403")
    @MethodSource("rotasProtegidas")
    void tokenDeOutroUsuario_devolve403(Rota rota) throws Exception {
        MvcResult res = chamar(rota, outro.id(), dono.token());

        assertThat(res.getResponse().getStatus()).isEqualTo(403);
        assertThat(corpoJson(res).path("codigo").asText()).isEqualTo("ACESSO_NEGADO");
    }

    @ParameterizedTest(name = "{0}: o dono devolve 200")
    @MethodSource("rotasProtegidas")
    void dono_devolve200(Rota rota) throws Exception {
        MvcResult res = chamar(rota, dono.id(), dono.token());

        assertThat(res.getResponse().getStatus()).as(res.getResponse().getContentAsString()).isEqualTo(200);
        assertThat(corpoJson(res).path("success").asBoolean()).isTrue();
    }

    // ── rotas sem usuarioId ou com particularidade ──────────────────────

    @Test
    void rota_semToken_devolve401() throws Exception {
        MvcResult res = mvc.perform(post("/api/rota").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("origem", List.of(-48.5, -27.6), "destino", List.of(-48.4, -27.5)))).andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void rota_comToken_passaDaAutorizacao() throws Exception {
        MvcResult res = mvc.perform(post("/api/rota").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + dono.token())
                .content(corpo("origem", List.of(-48.5, -27.6), "destino", List.of(-48.4, -27.5)))).andReturn();

        // Sem ORS_API_KEY o controller responde 503 sem chamar a ORS: o que importa é que não é 401/403.
        assertThat(res.getResponse().getStatus()).isEqualTo(503);
        assertThat(corpoJson(res).path("erro").asText()).isEqualTo("ROTA_INDISPONIVEL");
    }

    @Test
    void mentoriaStatus_semToken401_outroUsuario403_dono200() throws Exception {
        MvcResult criada = chamar(rotaMentoria(), dono.id(), dono.token());
        long solicitacaoId = corpoJson(criada).path("data").path("id").asLong();
        String caminho = "/api/mentorias/" + solicitacaoId + "/status";
        String corpo = corpo("status", "CONFIRMADA");

        assertThat(patch(caminho, corpo, null)).isEqualTo(401);
        assertThat(patch(caminho, corpo, outro.token())).isEqualTo(403);
        assertThat(patch(caminho, corpo, dono.token())).isEqualTo(200);
    }

    @Test
    void historicoDeSaude_semParametro_usaOUsuarioDoToken() throws Exception {
        mvc.perform(post("/api/saude").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + dono.token())
                .content(corpo("nota", 7, "contexto", "sem usuarioId no corpo", "idioma", "pt"))).andReturn();

        MvcResult res = mvc.perform(get("/api/saude/historico")
                .header("Authorization", "Bearer " + dono.token())).andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).contains("sem usuarioId no corpo");
    }

    @Test
    void historicoDeSaude_doDono_naoApareceParaOutroUsuario() throws Exception {
        mvc.perform(post("/api/saude").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + dono.token())
                .content(corpo("nota", 5, "contexto", "relato-so-do-dono", "idioma", "pt"))).andReturn();

        MvcResult res = mvc.perform(get("/api/saude/historico")
                .header("Authorization", "Bearer " + outro.token())).andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);
        assertThat(res.getResponse().getContentAsString()).doesNotContain("relato-so-do-dono");
    }

    @Test
    void tokenInvalido_devolve401() throws Exception {
        MvcResult res = mvc.perform(get("/api/saude/historico")
                .header("Authorization", "Bearer isto-nao-e-um-jwt")).andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void tokenRevogadoNoLogout_devolve401() throws Exception {
        Conta efemera = registrar("efemera");
        assertThat(mvc.perform(get("/api/saude/historico")
                .header("Authorization", "Bearer " + efemera.token())).andReturn().getResponse().getStatus()).isEqualTo(200);

        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + efemera.token())).andReturn();

        assertThat(mvc.perform(get("/api/saude/historico")
                .header("Authorization", "Bearer " + efemera.token())).andReturn().getResponse().getStatus()).isEqualTo(401);
    }

    // ── o que continua público ──────────────────────────────────────────

    @Test
    void catalogoEEstaticosContinuamPublicos() throws Exception {
        List<String> publicas = List.of(
            "/api/vagas", "/api/vagas/regioes", "/api/vagas/stats", "/api/vagas/" + vagaId,
            "/api/cursos", "/api/cursos/regioes", "/api/cursos/gratuitos", "/api/cursos/beneficentes",
            "/api/cursos/stats", "/api/cursos/" + cursoId,
            "/api/roadmaps/Java", "/api/lazer/pontos", "/api/mentores",
            "/manifest.json", "/sw.js");

        for (String caminho : publicas) {
            int status = mvc.perform(get(caminho)).andReturn().getResponse().getStatus();
            assertThat(status).as("GET %s sem token", caminho).isEqualTo(200);
        }
    }

    @Test
    void registroELoginContinuamPublicos() throws Exception {
        Conta nova = registrar("publico");
        String email = "login_publico_" + System.nanoTime() + "@teste.com";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("nome", "Login Publico", "email", email, "password", "123456"))).andReturn();

        MvcResult login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("email", email, "password", "123456"))).andReturn();

        assertThat(nova.token()).isNotBlank();
        assertThat(login.getResponse().getStatus()).isEqualTo(200);
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private Rota rotaMentoria() {
        return new Rota("POST /api/mentorias", HttpMethod.POST, id -> "/api/mentorias?usuarioId=" + id,
            id -> corpo("mentorId", mentorId, "areaSolicitada", "Java", "mensagem", "status"));
    }

    private int patch(String caminho, String corpo, String token) throws Exception {
        MockHttpServletRequestBuilder req = request(HttpMethod.PATCH, caminho)
                .contentType(MediaType.APPLICATION_JSON).content(corpo);
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(req).andReturn().getResponse().getStatus();
    }

    private MvcResult chamar(Rota rota, Long usuarioIdEnviado, String token) throws Exception {
        MockHttpServletRequestBuilder req = request(rota.metodo(), rota.caminho().apply(usuarioIdEnviado));
        String corpo = rota.corpo().apply(usuarioIdEnviado);
        if (corpo != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(req).andReturn();
    }

    private Conta registrar(String prefixo) throws Exception {
        String email = prefixo + "_" + System.nanoTime() + "@teste.com";
        MvcResult res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("nome", "Usuario " + prefixo, "email", email, "password", "123456",
                        "cidade", "TRINDADE", "areaTecnologia", "Java", "idioma", "pt"))).andReturn();
        assertThat(res.getResponse().getStatus()).isEqualTo(201);
        JsonNode data = corpoJson(res).path("data");
        return new Conta(data.path("userId").asLong(), data.path("token").asText());
    }

    private Long primeiroIdDe(String caminhoPublico) throws Exception {
        MvcResult res = mvc.perform(get(caminhoPublico)).andReturn();
        return corpoJson(res).path("data").get(0).path("id").asLong();
    }

    private JsonNode corpoJson(MvcResult res) throws Exception {
        return json.readTree(res.getResponse().getContentAsString());
    }

    /** Monta um JSON a partir de pares chave, valor. */
    private String corpo(Object... paresChaveValor) {
        try {
            Map<String, Object> mapa = new java.util.LinkedHashMap<>();
            for (int i = 0; i < paresChaveValor.length; i += 2) {
                mapa.put((String) paresChaveValor[i], paresChaveValor[i + 1]);
            }
            return json.writeValueAsString(mapa);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
