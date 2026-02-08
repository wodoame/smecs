package com.smecs.service;

import com.smecs.entity.OrderItem;
import com.smecs.dto.OrderItemDTO;
import java.util.List;

public interface OrderItemService {
    OrderItem saveOrderItem(OrderItem orderItem);
    List<OrderItem> createOrderItems(Long orderId, List<OrderItemDTO> orderItemDTOs);
    java.util.Optional<OrderItem> getOrderItemById(Long orderItemId);
    List<OrderItem> getOrderItemsByOrderId(Long orderId);
    OrderItem updateOrderItem(Long orderItemId, OrderItemDTO orderItemDTO);
    void deleteOrderItem(Long orderItemId);
}
