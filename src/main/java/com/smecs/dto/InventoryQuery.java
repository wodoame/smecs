package com.smecs.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class InventoryQuery {
    @Builder.Default
    private final String query = "";

    @Builder.Default
    private final Integer page = 1;

    @Builder.Default
    private final Integer size = 10;

    @Builder.Default
    private final String sort = "id,asc";
}

