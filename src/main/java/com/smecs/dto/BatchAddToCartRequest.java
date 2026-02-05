package com.smecs.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

@Data
public class BatchAddToCartRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<CartItemRequest> items;
}
