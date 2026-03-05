package com.smecs.dto;

import com.smecs.entity.SecurityEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventCountDTO {
    private SecurityEventType eventType;
    private long count;
}

