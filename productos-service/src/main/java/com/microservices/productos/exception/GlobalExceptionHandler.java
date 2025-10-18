package com.microservices.productos.exception;

import com.microservices.productos.model.jsonapi.JsonApiError;
import com.microservices.productos.model.jsonapi.JsonApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<JsonApiResponse<Void>> handleProductoNotFound(ProductoNotFoundException ex) {
        log.error("Producto no encontrado: {}", ex.getMessage());

        JsonApiError error = JsonApiError.builder()
                .status("404")
                .title("Recurso no encontrado")
                .detail(ex.getMessage())
                .build();

        JsonApiResponse<Void> response = JsonApiResponse.<Void>builder()
                .errors(List.of(error))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("Error de validación: {}", ex.getMessage());

        List<JsonApiError> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            JsonApiError jsonApiError = JsonApiError.builder()
                    .status("400")
                    .title("Error de validación")
                    .detail(error.getField() + ": " + error.getDefaultMessage())
                    .build();
            errors.add(jsonApiError);
        });

        JsonApiResponse<Void> response = JsonApiResponse.<Void>builder()
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);

        JsonApiError error = JsonApiError.builder()
                .status("500")
                .title("Error interno del servidor")
                .detail("Ocurrió un error inesperado")
                .build();

        JsonApiResponse<Void> response = JsonApiResponse.<Void>builder()
                .errors(List.of(error))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
