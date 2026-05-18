package com.example.ms_subscriptions.exception.custom;

public class SubscriptionNotActiveException extends RuntimeException {
    public SubscriptionNotActiveException(String message) {
        super(message);
    }
}