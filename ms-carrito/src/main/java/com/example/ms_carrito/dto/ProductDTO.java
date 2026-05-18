package com.example.ms_carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDTO {
    private Long id;
    private String nombre;
    private String categoria;
    private Double precio;
    private String descripcion;
    //private String imagenUrl;
    private Integer stock;
}