package com.example.ms_pagos.dto.response;

import com.example.ms_pagos.model.PagosMethod;
import com.example.ms_pagos.model.PagosStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Respuesta con los datos del pago procesado")
public class PagosResponseDTO extends RepresentationModel<PagosResponseDTO> {

    @Schema(description = "ID del pago", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long paymentId;

    @Schema(description = "ID de la orden asociada", example = "1")
    private Long orderId;

    @Schema(description = "ID del usuario", example = "1")
    private Long userId;

    @Schema(description = "Monto pagado", example = "99.99")
    private Double amount;

    @Schema(description = "Método de pago usado", example = "CREDIT_CARD")
    private PagosMethod method;

    @Schema(description = "Estado del pago", example = "COMPLETED", accessMode = Schema.AccessMode.READ_ONLY)
    private PagosStatus status;

    @Schema(description = "ID de transacción", example = "TXN-123456", accessMode = Schema.AccessMode.READ_ONLY)
    private String transactionId;

    @Schema(description = "Fecha de creación del pago", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de completado del pago", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime completedAt;

    @Schema(description = "Mensaje de error si el pago falló", example = "Fondos insuficientes")
    private String errorMessage;
}