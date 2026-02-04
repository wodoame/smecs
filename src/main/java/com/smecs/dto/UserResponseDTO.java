package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Long authExpiry;
}
