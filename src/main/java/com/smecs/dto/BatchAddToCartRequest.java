package com.smecs.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchAddToCartRequest {
    private Long userId;
    private List<CartItemRequest> items;
}
