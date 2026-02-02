package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private CategoryDTO category;
    private String imageUrl;
}
