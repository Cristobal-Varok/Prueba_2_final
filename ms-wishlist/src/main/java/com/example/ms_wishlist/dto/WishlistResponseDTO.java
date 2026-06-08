package com.example.ms_wishlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta con datos del item en wishlist")
public class WishlistResponseDTO extends RepresentationModel<WishlistResponseDTO> {

    @Schema(description = "ID único del item", example = "1", accessMode = READ_ONLY)
    private Long id;

    @Schema(description = "ID del producto", example = "prod-123", requiredMode = REQUIRED)
    private String productId;

    @Schema(description = "Nombre del producto", example = "The Legend of Zelda")
    private String productName;

    @Schema(description = "Tipo de producto", example = "GAME", allowableValues = {"GAME", "COMIC"})
    private String productType;

    @Schema(description = "Precio del producto", example = "59.99")
    private Double productPrice;

    @Schema(description = "URL de la imagen", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "Fecha de agregado", example = "2026-06-08T10:30:00", accessMode = READ_ONLY)
    private LocalDateTime addedAt;
}