package com.microservices.inventario.exception;

import com.microservices.inventario.exception.InventarioNotFoundException;
import com.microservices.inventario.exception.ProductoServiceException;
import com.microservices.inventario.model.jsonapi.JsonApiError;
import com.microservices.inventario.model.jsonapi.JsonApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InventarioNotFoundException.class)
    public ResponseEntity<JsonApiResponse<Void>> handleInventarioNotFound(
            InventarioNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Inventario no encontrado: {}", ex.getMessage());

        JsonApiError error = JsonApiError.builder()
                .status(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .title("Inventario no encontrado")
                .detail(ex.getMessage())
                .build();

        JsonApiResponse<Void> response = JsonApiResponse.<Void>builder()
                .errors(List.of(error))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ProductoServiceException.class)
    public ResponseEntity<JsonApiResponse<Void>> handleProductoNotFound(
            ProductoServiceException ex,
            HttpServletRequest request) {

        log.warn("Producto no encontrado: {}", ex.getMessage());

        JsonApiError error = JsonApiError.builder()
                .status(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .title("Producto no encontrado")
                .detail(ex.getMessage())
                .build();

        JsonApiResponse<Void> response = JsonApiResponse.<Void>builder()
                .errors(List.of(error))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<JsonApiResponse<Void>> handleRestClientException(
            RestClientException ex,
            HttpServletRequest request) {

        log.error("Error de comunicación con servicio externo: {}", ex.getMessage());

        JsonApiError error = JsonApiError.builder()
                .status(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()))
                .title("Servicio no disponible")
                .detail("Error al comunicarse con el servicio de productos")
                .build();

        JsonApiResponse<Void> response = JsonApiResponse.<Void>builder()
                .errors(List.of(error))
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Error de validación: {}", errorMsg);

        JsonApiError error = JsonApiError.builder()
                .status(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .title("Error de validación")
                .detail(errorMsg)
                .build();

        JsonApiResponse<Void> response = JsonApiResponse.<Void>builder()
                .errors(List.of(error))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Error inesperado: ", ex);
        
        JsonApiError error = JsonApiError.builder()
                .status(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .title("Error interno del servidor")
                .detail("Ocurrió un error inesperado al procesar la solicitud")
                .build();

        JsonApiResponse<Void> response = JsonApiResponse.<Void>builder()
                .errors(List.of(error))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
