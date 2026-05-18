package com.example.ms_tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketDTO {

    @NotBlank(message = "Asunto es requerido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String subject;

    @NotBlank(message = "Descripción es requerida")
    @Size(max = 2000, message = "Máximo 2000 caracteres")
    private String description;
}