package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.AuthDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsuarioControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private AuthDto.Response registrarUsuario(String prefixo) {
        String email = prefixo + "_" + System.currentTimeMillis() + "@teste.com";
        var request = new AuthDto.RegisterRequest(
                "Usuario Teste", email, "123456", "TRINDADE", null,
                "Estudante", "Java", "HTML, CSS");
        ResponseEntity<StandardApiResponse<AuthDto.Response>> response = restTemplate.exchange(
                "/api/auth/register", HttpMethod.POST, new HttpEntity<>(request),
                new ParameterizedTypeReference<StandardApiResponse<AuthDto.Response>>() {});
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().data();
    }

    private HttpHeaders headersComToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    @Test
    void shouldUpdateLocalizacaoSuccessfully() {
        AuthDto.Response usuario = registrarUsuario("loc_ok");

        var body = new com.bitsystem.bitapp.dto.UsuarioDto.LocalizacaoRequest(-27.5954, -48.5480);
        HttpEntity<Object> entity = new HttpEntity<>(body, headersComToken(usuario.token()));

        ResponseEntity<StandardApiResponse> response = restTemplate.exchange(
                "/api/usuarios/" + usuario.userId() + "/localizacao",
                HttpMethod.PUT, entity, StandardApiResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
    }

    @Test
    void shouldRejectInvalidLatitude() {
        AuthDto.Response usuario = registrarUsuario("loc_lat_invalida");

        var body = new com.bitsystem.bitapp.dto.UsuarioDto.LocalizacaoRequest(999.0, -48.5480);
        HttpEntity<Object> entity = new HttpEntity<>(body, headersComToken(usuario.token()));

        ResponseEntity<StandardApiResponse> response = restTemplate.exchange(
                "/api/usuarios/" + usuario.userId() + "/localizacao",
                HttpMethod.PUT, entity, StandardApiResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
    }

    @Test
    void shouldRejectOwnershipMismatch() {
        AuthDto.Response usuarioA = registrarUsuario("loc_owner_a");
        AuthDto.Response usuarioB = registrarUsuario("loc_owner_b");

        var body = new com.bitsystem.bitapp.dto.UsuarioDto.LocalizacaoRequest(-27.5954, -48.5480);
        // Token de A tentando atualizar a localização de B.
        HttpEntity<Object> entity = new HttpEntity<>(body, headersComToken(usuarioA.token()));

        ResponseEntity<StandardApiResponse> response = restTemplate.exchange(
                "/api/usuarios/" + usuarioB.userId() + "/localizacao",
                HttpMethod.PUT, entity, StandardApiResponse.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("ACESSO_NEGADO", response.getBody().codigo());
    }
}
