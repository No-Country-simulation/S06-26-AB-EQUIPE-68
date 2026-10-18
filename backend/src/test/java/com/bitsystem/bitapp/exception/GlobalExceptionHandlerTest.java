package com.bitsystem.bitapp.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.bitsystem.bitapp.dto.AuthDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GlobalExceptionHandlerTest {

    @Autowired TestRestTemplate rest;

    @Test
    void erroInesperado_devolveHttp500_comOMesmoCorpoDeAntes() {
        ResponseEntity<StandardApiResponse<Void>> resposta =
                new GlobalExceptionHandler().handleGeneral(new IllegalStateException("falha qualquer"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().success()).isFalse();
        assertThat(resposta.getBody().codigo()).isEqualTo("ERRO_INTERNO");
        assertThat(resposta.getBody().error())
                .isEqualTo("Serviço temporariamente indisponível. Tente novamente em alguns instantes.");
    }

    @Test
    void jsonMalformadoNoCorpo_eErroDoCliente_400_nao500() {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resposta = rest.exchange("/api/auth/login", HttpMethod.POST,
                new HttpEntity<>("{isto não é json", cabecalhos), Map.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).containsEntry("success", false).containsEntry("codigo", "REQUISICAO_INVALIDA");
    }

    @Test
    void metodoNaoAceito_eRotaInexistente_saoErrosDoCliente_nao500() {
        var cadastro = new AuthDto.RegisterRequest("Teste Erro", "erro-" + System.nanoTime() + "@teste.com", "123456",
                "TRINDADE", null, "Estudante", "Java", "HTML", "pt");
        ResponseEntity<StandardApiResponse<AuthDto.Response>> registro = rest.exchange(
                "/api/auth/register", HttpMethod.POST, new HttpEntity<>(cadastro),
                new ParameterizedTypeReference<StandardApiResponse<AuthDto.Response>>() {});
        HttpHeaders autenticado = new HttpHeaders();
        autenticado.setBearerAuth(registro.getBody().data().token());

        // O token vale: a requisição passa pelo Spring Security e chega ao MVC.
        ResponseEntity<Map> metodo = rest.exchange("/api/auth/login", HttpMethod.GET, new HttpEntity<>(autenticado), Map.class);
        ResponseEntity<Map> rota = rest.exchange("/api/rota-que-nao-existe", HttpMethod.GET, new HttpEntity<>(autenticado), Map.class);

        assertThat(metodo.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(metodo.getBody()).containsEntry("codigo", "REQUISICAO_INVALIDA");
        assertThat(rota.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(rota.getBody()).containsEntry("codigo", "REQUISICAO_INVALIDA");
    }
}
