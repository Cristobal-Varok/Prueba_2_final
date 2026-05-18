package com.example.ms_users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserAddressDTO {

    @NotBlank(message = "La dirección es requerida")
    private String address;
}
