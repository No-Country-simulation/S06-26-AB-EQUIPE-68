package com.bitsystem.bitapp.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.bitsystem.bitapp.dto.AuthDto;
import com.bitsystem.bitapp.dto.MentorDto;
import com.bitsystem.bitapp.dto.SolicitacaoMentoriaDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;

/**
 * Testes end-to-end de Mentorias, contra o mesmo contexto/H2 usado pelo
 * MentorSeedService (roda uma vez no boot da suíte) — mesmo estilo de
 * UsuarioControllerTest. Todas as rotas de mentorias exigem login e o
 * usuário vem do token; a matriz 401/403/200 de cada rota fica em
 * AutorizacaoRotasTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MentoriaControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // SimpleClientHttpRequestFactory (padrão do TestRestTemplate) não suporta
    // PATCH (HttpURLConnection); troca pelo cliente HTTP do JDK 11+, que suporta.
    @BeforeEach
    void habilitarPatch() {
        restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    private AuthDto.Response registrarUsuario(String prefixo) {
        String email = prefixo + "_" + System.nanoTime() + "@teste.com";
        var request = new AuthDto.RegisterRequest(
                "Usuario Teste", email, "123456", "TRINDADE", null,
                "Estudante", "Java", "HTML, CSS", "pt");
        ResponseEntity<StandardApiResponse<AuthDto.Response>> response = restTemplate.exchange(
                "/api/auth/register", HttpMethod.POST, new HttpEntity<>(request),
                new ParameterizedTypeReference<StandardApiResponse<AuthDto.Response>>() {});
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response.getBody().data();
    }

    private HttpHeaders comToken(AuthDto.Response usuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + usuario.token());
        return headers;
    }

    private List<MentorDto> listarMentores() {
        ResponseEntity<StandardApiResponse<List<MentorDto>>> response = restTemplate.exchange(
                "/api/mentores", HttpMethod.GET, null,
                new ParameterizedTypeReference<StandardApiResponse<List<MentorDto>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().data();
    }

    private SolicitacaoMentoriaDto.Response solicitar(AuthDto.Response usuario, Long mentorId, String area, String mensagem) {
        ResponseEntity<StandardApiResponse<SolicitacaoMentoriaDto.Response>> response = restTemplate.exchange(
                "/api/mentorias", HttpMethod.POST,
                new HttpEntity<>(new SolicitacaoMentoriaDto.Request(mentorId, area, mensagem), comToken(usuario)),
                new ParameterizedTypeReference<StandardApiResponse<SolicitacaoMentoriaDto.Response>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody().data();
    }

    @Test
    void listarMentores_retornaOsQuatroMentoresDoSeed() {
        List<MentorDto> mentores = listarMentores();
        assertEquals(4, mentores.size());
    }

    @Test
    void solicitar_criaComStatusAguardandoConfirmacaoNoNomeDoDonoDoToken() {
        AuthDto.Response usuario = registrarUsuario("mentoria_nova");
        Long mentorId = listarMentores().get(0).id();

        SolicitacaoMentoriaDto.Response criada = solicitar(usuario, mentorId, "Java", "Quero orientação");

        assertEquals("AGUARDANDO_CONFIRMACAO", criada.status());
        assertEquals(usuario.userId(), criada.usuarioId());
    }

    @Test
    void historico_retornaOrdenadoMaisRecentePrimeiro() {
        AuthDto.Response usuario = registrarUsuario("mentoria_hist");
        Long mentorId = listarMentores().get(0).id();

        solicitar(usuario, mentorId, "Java", "primeira");
        solicitar(usuario, mentorId, "IA", "segunda");

        ResponseEntity<StandardApiResponse<List<SolicitacaoMentoriaDto.HistoricoResponse>>> response = restTemplate.exchange(
                "/api/mentorias/historico", HttpMethod.GET, new HttpEntity<>(comToken(usuario)),
                new ParameterizedTypeReference<StandardApiResponse<List<SolicitacaoMentoriaDto.HistoricoResponse>>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<SolicitacaoMentoriaDto.HistoricoResponse> historico = response.getBody().data();
        assertEquals(2, historico.size());
        assertEquals("IA", historico.get(0).areaSolicitada());
        assertEquals("Java", historico.get(1).areaSolicitada());
    }

    @Test
    void atualizarStatus_transicaoValida_avancaParaConfirmada() {
        AuthDto.Response usuario = registrarUsuario("mentoria_status");
        Long mentorId = listarMentores().get(0).id();
        Long id = solicitar(usuario, mentorId, "Java", "msg").id();

        ResponseEntity<StandardApiResponse<SolicitacaoMentoriaDto.Response>> response = restTemplate.exchange(
                "/api/mentorias/" + id + "/status", HttpMethod.PATCH,
                new HttpEntity<>(new SolicitacaoMentoriaDto.AtualizarStatusRequest("CONFIRMADA"), comToken(usuario)),
                new ParameterizedTypeReference<StandardApiResponse<SolicitacaoMentoriaDto.Response>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CONFIRMADA", response.getBody().data().status());
    }

    @Test
    void atualizarStatus_pulandoEtapa_retorna422() {
        AuthDto.Response usuario = registrarUsuario("mentoria_pulo");
        Long mentorId = listarMentores().get(0).id();
        Long id = solicitar(usuario, mentorId, "Java", "msg").id();

        ResponseEntity<StandardApiResponse> response = restTemplate.exchange(
                "/api/mentorias/" + id + "/status", HttpMethod.PATCH,
                new HttpEntity<>(new SolicitacaoMentoriaDto.AtualizarStatusRequest("CONCLUIDA"), comToken(usuario)),
                StandardApiResponse.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("TRANSICAO_INVALIDA", response.getBody().codigo());
    }
}
