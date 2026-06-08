package com.example.ms_tickets.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO para crear un nuevo ticket")
public class TicketDTO {

    @NotBlank(message = "Asunto es requerido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    @Schema(description = "Asunto del ticket", example = "Problema con mi pedido", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String subject;

    @NotBlank(message = "Descripción es requerida")
    @Size(max = 2000, message = "Máximo 2000 caracteres")
    @Schema(description = "Descripción detallada del problema", example = "No puedo acceder a mi cuenta", maxLength = 2000, requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
}