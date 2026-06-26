package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.AuthDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRegisterUser() {
        String email = "teste_" + System.currentTimeMillis() + "@teste.com";
        var request = new AuthDto.RegisterRequest(
                "Teste", email, "123456", "São Paulo", "11999999999",
                "Estudante", "Java", "HTML, CSS");
        ResponseEntity<StandardApiResponse> response = restTemplate
                .postForEntity("/api/auth/register", request, StandardApiResponse.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
    }

    @Test
    void shouldLoginUser() {
        String email = "login_" + System.currentTimeMillis() + "@teste.com";
        var registerRequest = new AuthDto.RegisterRequest(
                "Login", email, "123456", "SP", null, "Estudante", "Web", null);
        restTemplate.postForEntity("/api/auth/register", registerRequest, StandardApiResponse.class);

        var loginRequest = new AuthDto.LoginRequest(email, "123456");
        ResponseEntity<StandardApiResponse> response = restTemplate
                .postForEntity("/api/auth/login", loginRequest, StandardApiResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
    }
}
