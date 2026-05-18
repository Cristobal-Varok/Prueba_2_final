package com.example.ms_users.dto;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Email(message = "El email debe ser válido")
    private String mail;

    private String address;
}
