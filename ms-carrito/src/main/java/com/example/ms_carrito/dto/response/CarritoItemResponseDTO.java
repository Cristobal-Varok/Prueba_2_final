package com.example.ms_carrito.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Item dentro del carrito")
public class CarritoItemResponseDTO {

    @Schema(description = "ID del producto", example = "5")
    private Long productId;

    @Schema(description = "Nombre del producto", example = "Grand Theft Auto V")
    private String productName;

    @Schema(description = "Cantidad del producto", example = "2")
    private Integer quantity;

    @Schema(description = "Precio unitario del producto", example = "14.99")
    private Double unitPrice;

    @Schema(description = "Subtotal del item (cantidad x precio)", example = "29.98", accessMode = Schema.AccessMode.READ_ONLY)
    private Double subtotal;
}