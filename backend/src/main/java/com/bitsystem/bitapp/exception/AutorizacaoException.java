package com.bitsystem.bitapp.exception;

import org.springframework.http.HttpStatus;

/**
 * Falha de autorização com status HTTP próprio. Fica separada da BusinessException,
 * que o GlobalExceptionHandler sempre traduz em 422.
 */
public class AutorizacaoException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;

    private AutorizacaoException(HttpStatus status, String codigo, String message) {
        super(message);
        this.status = status;
        this.codigo = codigo;
    }

    /** 403: o token é válido, mas o recurso pertence a outro usuário. */
    public static AutorizacaoException acessoNegado(String message) {
        return new AutorizacaoException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", message);
    }

    /** 401: a rota exige um usuário autenticado e a requisição chegou sem ele. */
    public static AutorizacaoException naoAutenticado() {
        return new AutorizacaoException(HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", "Autenticação necessária.");
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }
}
