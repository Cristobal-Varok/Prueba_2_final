package com.example.ms_tickets.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketResponseDTO {

    @NotBlank(message = "Respuesta es requerida")
    private String adminResponse;

    private String status;  // OPCIONAL: EN_PROCESO, RESUELTO, CERRADO
}