package com.example.ms_envios.exception.custom;

public class OrderNotPaidException extends RuntimeException {
    public OrderNotPaidException(String message) {
        super(message);
    }
}