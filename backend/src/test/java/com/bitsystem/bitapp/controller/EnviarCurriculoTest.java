package com.bitsystem.bitapp.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.bitsystem.bitapp.domain.CandidaturaVaga;
import com.bitsystem.bitapp.dto.AuthDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.repository.CandidaturaVagaRepository;
import com.bitsystem.bitapp.repository.VagaRepository;
import java.util.List;
import java.util.Map;
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

/** O nome e o e-mail da candidatura são os do usuário do token, nunca os que vierem no corpo. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EnviarCurriculoTest {

    @Autowired TestRestTemplate rest;
    @Autowired VagaRepository vagaRepository;
    @Autowired CandidaturaVagaRepository candidaturaRepository;

    @Test
    void nomeEEmailDoCorpoSaoIgnorados_valemOsDoUsuarioDoToken() {
        String email = "curriculo-" + System.nanoTime() + "@teste.com";
        var cadastro = new AuthDto.RegisterRequest("Maria da Silva", email, "123456",
                "TRINDADE", null, "Estudante", "Java", "Java", "pt");
        AuthDto.Response usuario = rest.exchange("/api/auth/register", HttpMethod.POST, new HttpEntity<>(cadastro),
                new ParameterizedTypeReference<StandardApiResponse<AuthDto.Response>>() {}).getBody().data();
        Long vagaId = vagaRepository.findAll().get(0).getId();

        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setBearerAuth(usuario.token());
        Map<String, Object> corpo = Map.of(
                "vagaId", vagaId,
                "usuarioId", usuario.userId(),
                "nome", "Fulano Falso",
                "email", "falso@outro.com",
                "mensagem", "Tenho interesse");

        ResponseEntity<Map> resposta = rest.exchange("/api/vagas/enviar-curriculo", HttpMethod.POST,
                new HttpEntity<>(corpo, cabecalhos), Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CandidaturaVaga> candidaturas = candidaturaRepository.findByUsuarioIdOrderByDataEnvioDesc(usuario.userId());
        assertThat(candidaturas).hasSize(1);
        assertThat(candidaturas.get(0).getNome()).isEqualTo("Maria da Silva");
        assertThat(candidaturas.get(0).getEmail()).isEqualTo(email);
        assertThat(candidaturas.get(0).getMensagem()).isEqualTo("Tenho interesse");
        Map<?, ?> dados = (Map<?, ?>) resposta.getBody().get("data");
        assertThat(dados.get("nome")).isEqualTo("Maria da Silva");
        assertThat(dados.get("email")).isEqualTo(email);
    }
}
