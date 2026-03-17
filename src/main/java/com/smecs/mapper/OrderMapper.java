package com.smecs.mapper;

import com.smecs.dto.OrderDTO;
import com.smecs.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public OrderDTO toDTO(Order order) {
        if (order == null) return null;
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus() != null ? order.getStatus().toString() : null);
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}
