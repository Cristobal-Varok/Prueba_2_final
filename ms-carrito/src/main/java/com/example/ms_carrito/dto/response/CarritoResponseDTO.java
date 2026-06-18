package com.example.ms_carrito.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Respuesta con los datos del carrito del usuario")
public class CarritoResponseDTO extends RepresentationModel<CarritoResponseDTO> {

    @Schema(description = "ID del carrito", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long cartId;

    @Schema(description = "ID del usuario dueño del carrito", example = "10")
    private Long userId;

    @Schema(description = "Lista de items en el carrito")
    private List<CarritoItemResponseDTO> items;

    @Schema(description = "Total calculado del carrito", example = "29.99", accessMode = Schema.AccessMode.READ_ONLY)
    private Double total;

    @Schema(description = "Fecha de creación del carrito", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}