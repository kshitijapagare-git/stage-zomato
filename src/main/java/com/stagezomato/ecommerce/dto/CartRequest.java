package com.stagezomato.ecommerce.dto;

import com.stagezomato.ecommerce.entity.CartStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CartRequest(
        @NotBlank @Size(max = 100) String customerName,
        @NotNull CartStatus status
) {
}
