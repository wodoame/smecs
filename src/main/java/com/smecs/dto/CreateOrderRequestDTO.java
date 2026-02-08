package com.smecs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateOrderRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
}
