package com.bitsystem.bitapp.config;

import com.bitsystem.bitapp.exception.AutorizacaoException;
import org.springframework.security.core.Authentication;

/**
 * O usuário de uma requisição é sempre o dono do token. Um usuarioId que chega
 * por parâmetro, caminho ou corpo só é aceito quando coincide com ele.
 */
public final class UsuarioDoToken {

    private UsuarioDoToken() {
    }

    public static UsuarioAutenticado exigir(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        throw AutorizacaoException.naoAutenticado();
    }

    /**
     * Devolve o id do dono do token. Se o cliente informou um usuarioId e ele
     * é de outra pessoa, responde 403; se não informou, vale o do token.
     */
    public static Long resolver(Authentication authentication, Long usuarioIdInformado) {
        Long donoId = exigir(authentication).id();
        if (usuarioIdInformado != null && !donoId.equals(usuarioIdInformado)) {
            throw AutorizacaoException.acessoNegado("Você só pode acessar os seus próprios dados.");
        }
        return donoId;
    }
}
