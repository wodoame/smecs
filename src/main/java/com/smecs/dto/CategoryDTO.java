package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CategoryDTO {
    private Integer categoryId;
    private String categoryName;
    private String description;
    private String imageUrl;
    private List<String> relatedImageUrls;
}
