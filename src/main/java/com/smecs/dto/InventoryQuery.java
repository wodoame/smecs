package com.smecs.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class InventoryQuery {
    private final String query;
    private final Integer page;
    private final Integer size;
    private final String sort;
}

