package com.example.ms_tickets.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO para que el administrador responda a un ticket")
public class TicketResponseDTO {

    @NotBlank(message = "Respuesta es requerida")
    @Schema(description = "Respuesta del administrador", example = "Ticket recibido, estamos revisando...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String adminResponse;

    @Schema(description = "Nuevo estado del ticket (opcional)", example = "EN_PROCESO", allowableValues = {"ABIERTO", "EN_PROCESO", "RESUELTO", "CERRADO"})
    private String status;
}