package com.smecs.service;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.dto.UpdateOrderStatusRequestDTO;
import java.util.List;

public interface OrderService {
    OrderDTO createOrder(CreateOrderRequestDTO request);
    OrderDTO getOrderById(Long id);
    OrderDTO updateOrderStatus(Long id, UpdateOrderStatusRequestDTO request);
    List<OrderDTO> getAllOrders();
    void deleteOrder(Long id);
    void updateOrderTotalOrThrow(Long orderId);
}
