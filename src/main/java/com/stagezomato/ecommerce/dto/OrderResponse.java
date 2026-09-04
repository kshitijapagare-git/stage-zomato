package com.stagezomato.ecommerce.dto;

import com.stagezomato.ecommerce.entity.Order;
import com.stagezomato.ecommerce.entity.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        String customerName,
        Long productId,
        Integer quantity,
        BigDecimal unitPrice,
        OrderStatus status
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getProduct().getId(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getStatus()
        );
    }
}
