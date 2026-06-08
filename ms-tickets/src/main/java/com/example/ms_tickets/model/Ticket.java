package com.example.ms_tickets.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tickets")
@Schema(description = "Entidad que representa un ticket de soporte")
public class Ticket extends RepresentationModel<Ticket> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del ticket", example = "1", accessMode = READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nombre del usuario que creó el ticket", example = "javier", requiredMode = REQUIRED)
    private String username;

    @Column(nullable = false)
    @Schema(description = "Asunto del ticket", example = "Problema con mi pedido", maxLength = 100)
    private String subject;

    @Column(nullable = false, length = 2000)
    @Schema(description = "Descripción detallada del problema", example = "No puedo acceder a mi cuenta", maxLength = 2000)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado actual del ticket", example = "ABIERTO", allowableValues = {"ABIERTO", "EN_PROCESO", "RESUELTO", "CERRADO"})
    private TicketStatus status;

    @Column(nullable = false)
    @Schema(description = "Fecha de creación del ticket", example = "2026-06-08T10:30:00", accessMode = READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", example = "2026-06-08T14:30:00", accessMode = READ_ONLY)
    private LocalDateTime updatedAt;

    @Column(length = 2000)
    @Schema(description = "Respuesta del administrador", example = "Ticket recibido, estamos revisando...")
    private String adminResponse;
}