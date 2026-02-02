package com.smecs.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInventoryRequestDTO {
    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be zero or positive")
    private Integer quantity;

    @NotNull(message = "Product details are required")
    @Valid
    private ProductRequest product;

    @Getter
    @Setter
    public static class ProductRequest {
        @NotBlank(message = "Product name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private Double price;

        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be positive")
        private Long categoryId;

        private String imageUrl;
    }
}
