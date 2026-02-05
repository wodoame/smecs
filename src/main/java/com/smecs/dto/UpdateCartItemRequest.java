package com.smecs.dto;

import lombok.Data;

@Data
public class UpdateCartItemRequest {
    private Long userId;
    private Long cartItemId;
    private int quantity;
}
