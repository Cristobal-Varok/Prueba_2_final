package com.example.ms_order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true) // Añadido para HATEOAS
public class ProductDto extends RepresentationModel<ProductDto> { // Añadido el 'extends'
    private Long id;
    private String nombre;
    private String type;
    private Double precio;
    private String descripcion;
    //private String imagenUrl;
    private Integer stock;
}