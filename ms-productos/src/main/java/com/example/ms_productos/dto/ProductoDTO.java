package com.example.ms_productos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductoDTO {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre debe tener máximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo es requerido")
    @Pattern(regexp = "GAME|COMIC", message = "El tipo debe ser GAME o COMIC")
    private String categoria;

    @NotNull(message = "El precio es requerido")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    private String descripcion;

    //@NotBlank(message = "La URL de la imagen es requerida")
    //private String imagenUrl;

    @PositiveOrZero(message = "El stock debe ser mayor o igual a 0")
    private Integer stock;
}
