package com.stagezomato.ecommerce.dto;

import com.stagezomato.ecommerce.entity.CartItem;

public record CartItemResponse(
        Long id,
        Long cartId,
        Long productId,
        Integer quantity
) {
    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getCart().getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
        );
    }
}
