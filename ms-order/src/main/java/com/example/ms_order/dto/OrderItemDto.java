package com.example.ms_order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "DTO que representa un ítem de una orden")
public class OrderItemDto extends RepresentationModel<OrderItemDto> {

    @Schema(description = "Identificador del ítem", example = "1")
    private Long id;

    @Schema(description = "Cantidad del producto", example = "2")
    private Integer quantity;

    @Schema(description = "Precio unitario del producto", example = "75.50")
    private Double price;

    @Schema(description = "ID del producto", example = "101")
    private Long productId;

    @Schema(description = "Información del producto asociado")
    private ProductDto product;
}