package com.example.ms_pagos.dto.response;

import com.example.ms_pagos.model.PagosMethod;
import com.example.ms_pagos.model.PagosStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PagosResponseDTO {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Double amount;
    private PagosMethod method;
    private PagosStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
}