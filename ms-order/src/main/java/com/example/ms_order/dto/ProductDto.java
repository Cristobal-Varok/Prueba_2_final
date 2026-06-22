package com.example.ms_order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "DTO que representa un producto")
public class ProductDto extends RepresentationModel<ProductDto> {

    @Schema(description = "Identificador del producto", example = "101")
    private Long id;

    @Schema(description = "Nombre del producto", example = "Laptop Dell XPS")
    private String nombre;

    @Schema(description = "Tipo de producto", example = "ELECTRONICS")
    private String type;

    @Schema(description = "Precio del producto", example = "750.00")
    private Double precio;

    @Schema(description = "Descripción del producto", example = "Laptop de alta gama")
    private String descripcion;

    @Schema(description = "Stock disponible", example = "25")
    private Integer stock;
}