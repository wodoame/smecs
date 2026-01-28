package com.smecs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CategoryDTO {

    private final Integer categoryId;

    @Setter
    @NotBlank(message = "Category name must not be blank")
    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String categoryName;

    @Setter
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    public CategoryDTO(Integer categoryId, String categoryName, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
    }
}
