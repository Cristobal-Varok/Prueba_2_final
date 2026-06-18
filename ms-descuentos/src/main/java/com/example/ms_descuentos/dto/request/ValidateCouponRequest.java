package com.example.ms_descuentos.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Schema(description = "Datos para validar un cupón")
public class ValidateCouponRequest {

    @NotBlank
    @Schema(description = "Código del cupón a validar", example = "VERANO2026", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "ID del usuario que usa el cupón", example = "1")
    private Long userId; //para limitar x usuario

    @PositiveOrZero
    @Schema(description = "Total del carrito", example = "99.99")
    private Double cartTotal = 0.0;
}
