package com.example.ms_descuentos.dto.response;

import com.example.ms_descuentos.model.DescuentosType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Respuesta con los datos de un cupón de descuento")
public class DescuentosResponseDTO extends RepresentationModel<DescuentosResponseDTO> {

    @Schema(description = "ID del descuento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long discountId;

    @Schema(description = "Código del cupón", example = "VERANO2025")
    private String code;

    @Schema(description = "Descripción del cupón", example = "Descuento de verano")
    private String description;

    @Schema(description = "Tipo de descuento", example = "PERCENTAGE")
    private DescuentosType discountType;

    @Schema(description = "Valor del descuento", example = "10.0")
    private Double discountValue;

    @Schema(description = "Fecha de inicio de validez", example = "2025-01-01T00:00:00")
    private LocalDateTime validFrom;

    @Schema(description = "Fecha de fin de validez", example = "2025-12-31T23:59:59")
    private LocalDateTime validUntil;

    @Schema(description = "Máximo de usos permitidos", example = "100")
    private Integer maxUses;

    @Schema(description = "Usos actuales del cupón", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer currentUses;

    @Schema(description = "Monto mínimo de compra", example = "50.0")
    private Double minPurchaseAmount;

    @Schema(description = "Estado activo del cupón", example = "true")
    private Boolean active;

    @Schema(description = "IDs de productos aplicables", example = "101, 202, 303")
    private String applicableProductIds;
}