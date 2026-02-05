package com.smecs.dto;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    // Add more fields as needed (e.g., cartId, address, etc.)

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
