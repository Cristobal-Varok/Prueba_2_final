package com.example.ms_descuentos.dto.request;

import com.example.ms_descuentos.model.DescuentosType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Datos para crear un cupón de descuento")
public class CreateCouponRequest {

    @NotBlank
    @Schema(description = "Código único del cupón", example = "VERANO2026", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "Descripción del cupón", example = "Descuento de verano")
    private String description;

    @NotNull
    @Schema(description = "Tipo de descuento", example = "PERCENTAGE", requiredMode = Schema.RequiredMode.REQUIRED)
    private DescuentosType discountType;

    @NotNull
    @Positive
    @Schema(description = "Valor del descuento", example = "10.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double discountValue;

    @NotNull
    @Schema(description = "Fecha de inicio de validez", example = "2026-01-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime validFrom;

    @NotNull
    @Schema(description = "Fecha de fin de validez", example = "2026-12-31t23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime validUntil;

    @PositiveOrZero
    @Schema(description = "Máximo de usos permitidos (0 = ilimitado", example = "100")
    private Integer maxUses = 0;

    @PositiveOrZero
    @Schema(description = "Monto mínimo de compra para aplicar el cupón", example = "50.0")
    private Double minPurchaseAmount = 0.0;

    @Schema(description = "Estado activo del cupón", example = "true")
    private Boolean active = true;

    @Schema(description = "IDs de productos aplicables separados por coma", example = "101, 102, 103")
    private String applicableProductIds; //"101, 202, 303"
}
