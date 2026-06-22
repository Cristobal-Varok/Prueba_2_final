package com.example.ms_carrito.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos del producto obtenido desde ms-productos")
public class ProductDTO {

    @Schema(description = "ID del producto", example = "1")
    private Long id;

    @Schema(description = "Nombre del producto", example = "Spider-Man")
    private String nombre;

    @Schema(description = "Categoría del producto", example = "COMIC")
    private String categoria;

    @Schema(description = "Precio del producto", example = "14.99")
    private Double precio;

    @Schema(description = "Descripción del producto", example = "Cómic edición especial")
    private String descripcion;

    @Schema(description = "Stock disponible", example = "50")
    private Integer stock;
}