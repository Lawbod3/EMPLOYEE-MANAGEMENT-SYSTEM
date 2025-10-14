package com.darum.employee.exception;

import com.darum.shared.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles validation errors from @Valid annotated requests
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse>> handleValidationException(WebExchangeBindException ex) {
        String errorMessage = ex.getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, errorMessage)));
    }

    /**
     * Handles general runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ApiResponse>> handleRuntimeException(RuntimeException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, ex.getMessage())));
    }

    /**
     * Handles all other unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse>> handleGenericException(Exception ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Internal server error: " + ex.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiResponse>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, ex.getMessage())));
    }
}
