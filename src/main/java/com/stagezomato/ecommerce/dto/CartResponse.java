package com.stagezomato.ecommerce.dto;

import com.stagezomato.ecommerce.entity.Cart;
import com.stagezomato.ecommerce.entity.CartStatus;

public record CartResponse(
        Long id,
        String customerName,
        CartStatus status
) {
    public static CartResponse from(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getCustomerName(),
                cart.getStatus()
        );
    }
}
