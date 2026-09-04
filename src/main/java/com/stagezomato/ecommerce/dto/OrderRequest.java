package com.stagezomato.ecommerce.dto;

import com.stagezomato.ecommerce.entity.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderRequest(
        @NotBlank String customerName,
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal unitPrice,
        OrderStatus status
) {
}
