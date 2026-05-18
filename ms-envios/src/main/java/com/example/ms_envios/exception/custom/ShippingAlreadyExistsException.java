package com.example.ms_envios.exception.custom;

public class ShippingAlreadyExistsException extends RuntimeException {
    public ShippingAlreadyExistsException(String message) {
        super(message);
    }
}
