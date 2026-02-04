package com.smecs.service;

import com.smecs.entity.OrderItem;
import java.util.List;

public interface OrderItemService {
    OrderItem saveOrderItem(OrderItem orderItem);
    List<OrderItem> getOrderItemsByOrderId(Long orderId);
    void deleteOrderItem(Long orderItemId);
}
