package com.smecs.dto;

public class CreateOrderRequestDTO {
    private Long userId;
    // Add more fields as needed (e.g., cartId, address, etc.)

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
