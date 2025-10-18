package com.microservices.inventario.exception;

public class ProductoServiceException extends RuntimeException {
    public ProductoServiceException(String message) {
        super(message);
    }

    public ProductoServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
