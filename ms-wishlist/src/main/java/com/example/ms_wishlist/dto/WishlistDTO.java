package com.example.ms_wishlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO para agregar un producto a la wishlist")
public class WishlistDTO {

    @NotBlank(message = "ProductId es requerido")
    @Schema(description = "ID del producto", example = "prod-123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @NotBlank(message = "ProductName es requerido")
    @Schema(description = "Nombre del producto", example = "The Legend of Zelda", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    @NotBlank(message = "ProductType es requerido")
    @Schema(description = "Tipo de producto", example = "GAME", allowableValues = {"GAME", "COMIC"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String productType;

    @Schema(description = "Precio del producto", example = "59.99")
    private Double productPrice;

    @Schema(description = "URL de la imagen del producto", example = "https://example.com/image.jpg")
    private String imageUrl;
}