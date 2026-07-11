package com.example.ms_users.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "Respuesta de error para la API")
public class ErrorResponse {

    @Schema(description = "Código de estado HTTP", example = "404")
    private int status;

    @Schema(description = "Tipo de error", example = "Usuario no encontrado")
    private String error;

    @Schema(description = "Mensaje descriptivo del error", example = "Usuario no encontrado: juan.perez")
    private String message;

    @Schema(description = "Timestamp del error")
    private LocalDateTime timestamp;

    @Schema(description = "Errores de validación por campo")
    private Map<String, String> errors;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, Map<String, String> errors) {
        this.status = status;
        this.error = error;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }
}