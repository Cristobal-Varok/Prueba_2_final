package com.example.ms_productos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private String categoria;
    private Double precio;
    private String descripcion;
    //private String imagenUrl;
    private Integer stock;
}