package com.example.ms_wishlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WishlistDTO {

    @NotBlank(message = "ProductId es requerido")
    private String productId;

    @NotBlank(message = "ProductName es requerido")
    private String productName;

    @NotBlank(message = "ProductType es requerido")
    private String productType;  // "GAME" o "COMIC"

    private Double productPrice;

    private String imageUrl;
}
