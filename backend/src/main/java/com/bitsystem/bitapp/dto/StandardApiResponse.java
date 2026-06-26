package com.bitsystem.bitapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StandardApiResponse<T>(
    boolean success,
    T data,
    String error,
    String timestamp
) {
    public static <T> StandardApiResponse<T> ok(T data) {
        return new StandardApiResponse<>(true, data, null, LocalDateTime.now().toString());
    }

    public static <T> StandardApiResponse<T> error(String error) {
        return new StandardApiResponse<>(false, null, error, LocalDateTime.now().toString());
    }
}
