package com.example.ms_descuentos.dto.response;

import com.example.ms_descuentos.model.DescuentosType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DescuentosResponseDTO {
    private Long discountId;
    private String code;
    private String description;
    private DescuentosType discountType;
    private Double discountValue;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer maxUses;
    private Integer currentUses;
    private Double minPurchaseAmount;
    private Boolean active;
    private String applicableProductIds;
}