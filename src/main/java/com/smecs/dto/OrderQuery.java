package com.smecs.dto;

import com.smecs.entity.Order;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class OrderQuery {
    private final Integer page;
    private final Integer size;
    private final String sort;
    private final Order.Status status;
}
