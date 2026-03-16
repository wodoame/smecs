package com.smecs.service;

import com.smecs.entity.OrderItem;
import com.smecs.dto.OrderItemDTO;
import java.util.List;

public interface OrderItemService {
    List<OrderItem> createOrderItems(List<OrderItemDTO> orderItemDTOs);
    java.util.Optional<OrderItem> getOrderItemById(Long orderItemId);
    List<OrderItem> getOrderItemsByOrderId(Long orderId);
    OrderItem updateOrderItem(Long orderItemId, OrderItemDTO orderItemDTO);
    void deleteOrderItem(Long orderItemId);
}
