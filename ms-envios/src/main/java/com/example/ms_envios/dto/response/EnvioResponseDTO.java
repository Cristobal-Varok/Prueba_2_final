package com.example.ms_envios.dto.response;

import com.example.ms_envios.model.EnviosStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EnvioResponseDTO {
    private Long shippingId;
    private Long orderId;
    private Long userId;
    private String address;
    private EnviosStatus status;
    private String trackingNumber;
    private LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime deliveredAt;
}