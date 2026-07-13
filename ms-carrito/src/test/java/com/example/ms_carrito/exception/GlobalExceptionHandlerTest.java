package com.example.ms_carrito.exception;

import com.example.ms_carrito.exception.custom.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deberiaManejarCartNotFoundException() {
        ResponseEntity<ErrorResponse> response = handler.handleCartNotFound(new CartNotFoundException("Carrito 1 no existe"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void deberiaManejarItemNotFoundException() {
        ResponseEntity<ErrorResponse> response = handler.handleItemNotFound(new ItemNotFoundException("Item no existe"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deberiaManejarInsufficientStockException() {
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientStock(new InsufficientStockException("Sin stock"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void deberiaManejarProductNotFoundException() {
        ResponseEntity<ErrorResponse> response = handler.handleProductNotFound(new ProductNotFoundException("Producto no existe"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deberiaManejarUserNotFoundException() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(new UserNotFoundException("Usuario no existe"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deberiaManejarAccessDeniedException() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(new AccessDeniedException("Denegado"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void deberiaManejarIllegalArgumentException() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(new IllegalArgumentException("Argumento inválido"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deberiaManejarRuntimeException() {
        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(new RuntimeException("Error inesperado"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deberiaManejarGenericException() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(new Exception("Error genérico"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ha ocurrido un error inesperado", response.getBody().getMessage());
    }

    @Test
    void deberiaManejarValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());

        ErrorResponse response = handler.handleValidationExceptions(ex);
        assertEquals(400, response.getStatus());
    }
}