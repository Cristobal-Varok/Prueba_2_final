package com.example.ms_envios.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "DTO de respuesta para errores")
public class ErrorResponse {

    @Schema(description = "Código de estado HTTP", example = "404")
    private int status;

    @Schema(description = "Tipo de error", example = "No encontrado")
    private String error;

    @Schema(description = "Mensaje descriptivo del error", example = "Envío no encontrado con ID: 1")
    private String message;

    @Schema(description = "Fecha y hora del error", example = "2026-06-08T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Mapa de errores de validación")
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