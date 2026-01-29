package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CategoryDTO {
    private Integer categoryId;
    private String categoryName;
    private String description;

    public CategoryDTO() {}
    public CategoryDTO(Integer categoryId, String categoryName, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
    }
}
