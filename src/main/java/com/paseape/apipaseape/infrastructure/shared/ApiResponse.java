package com.paseape.apipaseape.infrastructure.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> build(int statusCode, String code, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(String.valueOf(statusCode))
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> build(String code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}

