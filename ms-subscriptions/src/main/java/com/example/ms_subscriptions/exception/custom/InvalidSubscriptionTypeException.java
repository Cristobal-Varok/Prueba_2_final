package com.example.ms_subscriptions.exception.custom;

public class InvalidSubscriptionTypeException extends RuntimeException {
    public InvalidSubscriptionTypeException(String message) {
        super(message);
    }
}
