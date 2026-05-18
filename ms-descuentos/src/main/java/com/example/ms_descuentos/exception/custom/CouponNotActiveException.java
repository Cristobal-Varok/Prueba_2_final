package com.example.ms_descuentos.exception.custom;

public class CouponNotActiveException extends RuntimeException {
    public CouponNotActiveException(String message) {
        super(message);
    }
}
