package com.example.ms_descuentos.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado de la validación o uso de un cupón")
public class DescuentosResult extends RepresentationModel<DescuentosResult> {

    @Schema(description = "Indica si el cupón es válido", example = "true")
    private boolean valid;

    @Schema(description = "Monto de descuento aplicado", example = "9.99")
    private Double discountAmount;

    @Schema(description = "Mensaje descriptivo del resultado", example = "Cupón aplicado correctamente")
    private String message;

    @Schema(description = "Código del cupón usado", example = "VERANO2025")
    private String couponCode;
}