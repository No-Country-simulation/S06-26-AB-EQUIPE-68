package com.bitsystem.bitapp.config;

import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * Dono do token, como o jwtAuthenticationFilter o coloca no SecurityContext.
 * getName() devolve o e-mail, então authentication.getName() segue valendo
 * para quem já o usa (AuthController, VagaController, CursoController).
 */
public record UsuarioAutenticado(Long id, String email) implements AuthenticatedPrincipal {

    @Override
    public String getName() {
        return email;
    }
}
