package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryDTO {
    private Long id;
    private ProductDTO product;
    private Integer quantity;
}
