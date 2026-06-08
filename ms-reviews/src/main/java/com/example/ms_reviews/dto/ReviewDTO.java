package com.example.ms_reviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "DTO para crear o actualizar una reseña")
public class ReviewDTO {

    @NotBlank(message = "ProductId es requerido")
    @Schema(description = "ID del producto", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @NotNull(message = "Rating es requerido")
    @Min(value = 1, message = "Rating mínimo 1")
    @Max(value = 5, message = "Rating máximo 5")
    @Schema(description = "Calificación (1-5 estrellas)", example = "5", minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @NotBlank(message = "Comment es requerido")
    @Size(max = 1000, message = "Máximo 1000 caracteres")
    @Schema(description = "Comentario de la reseña", example = "Excelente producto, muy recomendado", maxLength = 1000, requiredMode = Schema.RequiredMode.REQUIRED)
    private String comment;

    @NotBlank(message = "ProductType es requerido")
    @Pattern(regexp = "GAME|COMIC", message = "ProductType debe ser GAME o COMIC")
    @Schema(description = "Tipo de producto", example = "GAME", allowableValues = {"GAME", "COMIC"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String productType;
}