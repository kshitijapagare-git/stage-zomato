package com.stagezomato.ecommerce.exception;

public class OrderItemConflictException extends RuntimeException {
    public OrderItemConflictException(String message) {
        super(message);
    }
}
