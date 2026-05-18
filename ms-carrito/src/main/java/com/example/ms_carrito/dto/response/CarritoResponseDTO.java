package com.example.ms_carrito.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CarritoResponseDTO {
    private Long cartId;
    private Long userId;
    private List<CarritoItemResponseDTO> items;
    private Double total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}