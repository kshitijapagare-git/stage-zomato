package com.stagezomato.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull Long cartId,
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {
}
