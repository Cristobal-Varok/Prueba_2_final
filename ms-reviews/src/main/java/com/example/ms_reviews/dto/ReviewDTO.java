package com.example.ms_reviews.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewDTO {

    @NotBlank(message = "ProductId es requerido")
    private String productId;

    @NotNull(message = "Rating es requerido")
    @Min(value = 1, message = "Rating mínimo 1")
    @Max(value = 5, message = "Rating máximo 5")
    private Integer rating;

    @NotBlank(message = "Comment es requerido")
    @Size(max = 1000, message = "Máximo 1000 caracteres")
    private String comment;

    @NotBlank(message = "ProductType es requerido")
    @Pattern(regexp = "GAME|COMIC", message = "ProductType debe ser GAME o COMIC")
    private String productType;
}