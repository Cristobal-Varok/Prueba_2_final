package com.example.ms_reviews.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reviews")
@Schema(description = "Entidad que representa una reseña de un producto")
public class Review extends RepresentationModel<Review> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la reseña", example = "1", accessMode = READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nombre del usuario que escribió la reseña", example = "javier")
    private String username;

    @Column(nullable = false)
    @Schema(description = "ID del producto reseñado", example = "3")
    private String productId;

    @Column(nullable = false)
    @Schema(description = "Calificación (1-5 estrellas)", example = "5", minimum = "1", maximum = "5")
    private Integer rating;

    @Column(nullable = false, length = 1000)
    @Schema(description = "Comentario de la reseña", example = "Excelente producto, muy recomendado")
    private String comment;

    @Column(nullable = false)
    @Schema(description = "Fecha de creación de la reseña", example = "2026-06-08T10:30:00", accessMode = READ_ONLY)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Schema(description = "Tipo de producto", example = "GAME", allowableValues = {"GAME", "COMIC"})
    private String productType;
}