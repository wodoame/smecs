package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Setter
@Getter
public class CategoryDTO {
    private Integer categoryId;

    @NotBlank(message = "Category name is required")
    private String categoryName;
    private String description;
    private String imageUrl;
    private List<String> relatedImageUrls;
}
