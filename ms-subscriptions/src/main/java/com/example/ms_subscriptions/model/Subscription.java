package com.example.ms_subscriptions.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Entity
@Table(name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa una suscripción de usuario")
public class Subscription extends RepresentationModel<Subscription> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la suscripción", example = "1", accessMode = READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nombre del usuario", example = "javier")
    private String username;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Tipo de suscripción", example = "PREMIUM", allowableValues = {"BASICA", "PREMIUM", "VIP"})
    private SubscriptionType type;

    @Column(nullable = false)
    @Schema(description = "Fecha de inicio de la suscripción", example = "2026-06-08T10:30:00", accessMode = READ_ONLY)
    private LocalDateTime startDate;

    @Column(nullable = false)
    @Schema(description = "Fecha de fin de la suscripción", example = "2026-07-08T10:30:00")
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Schema(description = "Indica si la suscripción está activa", example = "true")
    private Boolean active;

    @Schema(description = "Fecha de cancelación (si aplica)", example = "2026-06-15T10:30:00")
    private LocalDateTime cancelledAt;
}