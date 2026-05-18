package com.example.ms_tickets.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;  // Usuario que crea el ticket (del token)

    @Column(nullable = false)
    private String subject;  // Asunto del ticket

    @Column(nullable = false, length = 2000)
    private String description;  // Descripción del problema

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus status;  // Estado del ticket

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String adminResponse;  // Respuesta del administrador
}