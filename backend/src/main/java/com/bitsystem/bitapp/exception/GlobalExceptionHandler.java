package com.bitsystem.bitapp.exception;

import com.bitsystem.bitapp.dto.StandardApiResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.warn("Business exception: {} - {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(StandardApiResponse.error(ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(AutorizacaoException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleAutorizacao(AutorizacaoException ex) {
        log.warn("Authorization exception: {} - {}", ex.getCodigo(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(StandardApiResponse.error(ex.getMessage(), ex.getCodigo()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<StandardApiResponse<List<String>>> handleValidation(ValidationException ex) {
        log.warn("Validation exception: {}", ex.getErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(StandardApiResponse.error(String.join("; ", ex.getErrors()), "VALIDACAO"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardApiResponse<List<String>>> handleMethodValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        log.warn("Method validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(StandardApiResponse.error(String.join("; ", errors), "VALIDACAO"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardApiResponse<Void>> handleGeneral(Exception ex) {
        // JSON ilegível, método não aceito, rota inexistente, parâmetro ausente: o próprio Spring já
        // sabe que é erro de quem chamou (4xx). Não é "erro inesperado" e não vira 500.
        HttpStatusCode doCliente = statusDeErroDoCliente(ex);
        if (doCliente != null) {
            log.warn("Requisição inválida ({}): {}", doCliente.value(), ex.getMessage());
            return ResponseEntity.status(doCliente)
                    .body(StandardApiResponse.error("Requisição inválida.", "REQUISICAO_INVALIDA"));
        }
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardApiResponse.error("Serviço temporariamente indisponível. Tente novamente em alguns instantes.", "ERRO_INTERNO"));
    }

    private static HttpStatusCode statusDeErroDoCliente(Exception ex) {
        if (ex instanceof ErrorResponse resposta && resposta.getStatusCode().is4xxClientError()) {
            return resposta.getStatusCode();
        }
        if (ex instanceof HttpMessageNotReadableException || ex instanceof TypeMismatchException) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }
}
