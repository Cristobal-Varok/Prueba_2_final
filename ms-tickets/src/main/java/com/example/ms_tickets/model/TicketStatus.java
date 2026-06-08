package com.example.ms_tickets.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de un ticket")
public enum TicketStatus {
    ABIERTO,
    EN_PROCESO,
    RESUELTO,
    CERRADO
}