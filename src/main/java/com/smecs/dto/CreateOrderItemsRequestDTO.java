package com.smecs.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderItemsRequestDTO {
    private List<OrderItemDTO> items;
}

