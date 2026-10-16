package com.bitsystem.bitapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class JwtUtilTest {

    private static final String SEGREDO_VALIDO = "segredo-aleatorio-de-teste-com-mais-de-32-bytes";

    private JwtUtil jwtUtil(String secret) {
        return new JwtUtil(secret, 60_000L, 120_000L);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void semSegredo_aAplicacaoNaoSobe_eAMensagemDizOQueFazer(String secret) {
        assertThatThrownBy(() -> jwtUtil(secret))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET não definido")
                .hasMessageContaining("openssl rand");
    }

    @Test
    void segredoMenorQue32Bytes_naoSobe() {
        String trintaEUm = "a".repeat(31);

        assertThatThrownBy(() -> jwtUtil(trintaEUm))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("curto demais")
                .hasMessageContaining("32");
    }

    @Test
    void segredoDeExactos32Bytes_sobe() {
        assertThatCode(() -> jwtUtil("a".repeat(32))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "dev-secret-only-change-in-production-min-512-bits-long-key-here",
        "bitapp-secret-key-change-in-production-512-bits-minimum",
        "CHANGE_ME_WITH_STRONG_SECRET_IN_PRODUCTION"})
    void segredosDeExemploPublicadosNoRepositorio_saoRejeitados(String publico) {
        assertThatThrownBy(() -> jwtUtil(publico))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exemplo publicado");
    }

    @Test
    void tokenGeradoCarregaIdEEmailDoDono() {
        JwtUtil jwt = jwtUtil(SEGREDO_VALIDO);

        String token = jwt.generateToken(42L, "ana@teste.com");

        assertThat(jwt.getUserIdFromToken(token)).isEqualTo(42L);
        assertThat(jwt.getEmailFromToken(token)).isEqualTo("ana@teste.com");
        assertThat(jwt.isTokenExpired(token)).isFalse();
    }

    @Test
    void tokenAssinadoComOutroSegredo_naoValida() {
        String token = jwtUtil(SEGREDO_VALIDO).generateToken(1L, "a@b.com");

        assertThatThrownBy(() -> jwtUtil("outro-segredo-aleatorio-com-mais-de-32-bytes").validateToken(token))
                .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }
}
