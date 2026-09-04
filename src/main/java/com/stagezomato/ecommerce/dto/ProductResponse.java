package com.stagezomato.ecommerce.dto;

import com.stagezomato.ecommerce.entity.Product;
import com.stagezomato.ecommerce.entity.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal price,
        Integer stock,
        ProductStatus status,
        Long categoryId
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                product.getCategory() != null ? product.getCategory().getId() : null
        );
    }
}
