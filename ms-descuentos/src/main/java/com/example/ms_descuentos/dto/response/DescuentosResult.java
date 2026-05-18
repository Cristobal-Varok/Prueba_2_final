package com.example.ms_descuentos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescuentosResult {
    private boolean valid;
    private Double discountAmount;
    private String message;
    private String couponCode;
}