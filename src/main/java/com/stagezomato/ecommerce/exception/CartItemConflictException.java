package com.stagezomato.ecommerce.exception;

public class CartItemConflictException extends RuntimeException {
    public CartItemConflictException(String message) {
        super(message);
    }
}
