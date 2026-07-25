package com.bitsystem.bitapp.controller;

import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;

/**
 * Testes end-to-end de Mentorias, contra o mesmo contexto/H2 usado pelo
 * MentorSeedService (roda uma vez no boot da suíte) — mesmo estilo de
 * UsuarioControllerTest.
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

    private List<MentorDto> listarMentores() {
        ResponseEntity<StandardApiResponse<List<MentorDto>>> response = restTemplate.exchange(
                "/api/mentores", HttpMethod.GET, null,
                new ParameterizedTypeReference<StandardApiResponse<List<MentorDto>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().data();
    }

    @Test
    void listarMentores_retornaOsQuatroMentoresDoSeed() {
        List<MentorDto> mentores = listarMentores();
        assertEquals(4, mentores.size());
    }

    @Test
    void solicitar_criaComStatusAguardandoConfirmacao() {
        Long mentorId = listarMentores().get(0).id();
        var request = new SolicitacaoMentoriaDto.Request(mentorId, "Java", "Quero orientação");

        ResponseEntity<StandardApiResponse<SolicitacaoMentoriaDto.Response>> response = restTemplate.exchange(
                "/api/mentorias?usuarioId=501", HttpMethod.POST, new HttpEntity<>(request),
                new ParameterizedTypeReference<StandardApiResponse<SolicitacaoMentoriaDto.Response>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertEquals("AGUARDANDO_CONFIRMACAO", response.getBody().data().status());
    }

    @Test
    void historico_retornaOrdenadoMaisRecentePrimeiro() {
        Long mentorId = listarMentores().get(0).id();
        Long usuarioId = 502L;

        restTemplate.exchange("/api/mentorias?usuarioId=" + usuarioId, HttpMethod.POST,
                new HttpEntity<>(new SolicitacaoMentoriaDto.Request(mentorId, "Java", "primeira")),
                new ParameterizedTypeReference<StandardApiResponse<SolicitacaoMentoriaDto.Response>>() {});
        restTemplate.exchange("/api/mentorias?usuarioId=" + usuarioId, HttpMethod.POST,
                new HttpEntity<>(new SolicitacaoMentoriaDto.Request(mentorId, "IA", "segunda")),
                new ParameterizedTypeReference<StandardApiResponse<SolicitacaoMentoriaDto.Response>>() {});

        ResponseEntity<StandardApiResponse<List<SolicitacaoMentoriaDto.HistoricoResponse>>> response = restTemplate.exchange(
                "/api/mentorias/historico?usuarioId=" + usuarioId, HttpMethod.GET, null,
                new ParameterizedTypeReference<StandardApiResponse<List<SolicitacaoMentoriaDto.HistoricoResponse>>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<SolicitacaoMentoriaDto.HistoricoResponse> historico = response.getBody().data();
        assertEquals(2, historico.size());
        assertEquals("IA", historico.get(0).areaSolicitada());
        assertEquals("Java", historico.get(1).areaSolicitada());
    }

    @Test
    void atualizarStatus_transicaoValida_avancaParaConfirmada() {
        Long mentorId = listarMentores().get(0).id();
        ResponseEntity<StandardApiResponse<SolicitacaoMentoriaDto.Response>> criada = restTemplate.exchange(
                "/api/mentorias?usuarioId=503", HttpMethod.POST,
                new HttpEntity<>(new SolicitacaoMentoriaDto.Request(mentorId, "Java", "msg")),
                new ParameterizedTypeReference<StandardApiResponse<SolicitacaoMentoriaDto.Response>>() {});
        Long id = criada.getBody().data().id();

        ResponseEntity<StandardApiResponse<SolicitacaoMentoriaDto.Response>> response = restTemplate.exchange(
                "/api/mentorias/" + id + "/status", HttpMethod.PATCH,
                new HttpEntity<>(new SolicitacaoMentoriaDto.AtualizarStatusRequest("CONFIRMADA")),
                new ParameterizedTypeReference<StandardApiResponse<SolicitacaoMentoriaDto.Response>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CONFIRMADA", response.getBody().data().status());
    }

    @Test
    void atualizarStatus_pulandoEtapa_retorna422() {
        Long mentorId = listarMentores().get(0).id();
        ResponseEntity<StandardApiResponse<SolicitacaoMentoriaDto.Response>> criada = restTemplate.exchange(
                "/api/mentorias?usuarioId=504", HttpMethod.POST,
                new HttpEntity<>(new SolicitacaoMentoriaDto.Request(mentorId, "Java", "msg")),
                new ParameterizedTypeReference<StandardApiResponse<SolicitacaoMentoriaDto.Response>>() {});
        Long id = criada.getBody().data().id();

        ResponseEntity<StandardApiResponse> response = restTemplate.exchange(
                "/api/mentorias/" + id + "/status", HttpMethod.PATCH,
                new HttpEntity<>(new SolicitacaoMentoriaDto.AtualizarStatusRequest("CONCLUIDA")),
                StandardApiResponse.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("TRANSICAO_INVALIDA", response.getBody().codigo());
    }
}
