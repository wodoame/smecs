package com.smecs.model;

import lombok.Getter;
import lombok.Setter;

// Removed JPA annotations to avoid entity name conflict
@Setter
@Getter
public class Category {
    private Integer categoryId;
    private String categoryName;
    private String description;

    public Category() {}

    public Category(int categoryId, String categoryName, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
    }

    @Override
    public String toString() {
        return categoryName; // Useful for ComboBox display
    }
}
