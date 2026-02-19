package com.smecs.service;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.dto.OrderQuery;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.UpdateOrderStatusRequestDTO;

public interface OrderService {
    OrderDTO createOrder(CreateOrderRequestDTO request);
    OrderDTO getOrderById(Long id);
    OrderDTO updateOrderStatus(Long id, UpdateOrderStatusRequestDTO request);
    PagedResponseDTO<OrderDTO> getAllOrders(OrderQuery query);
    PagedResponseDTO<OrderDTO> getOrdersByUserId(Long userId, OrderQuery query);
    void deleteOrder(Long id);
    void updateOrderTotalOrThrow(Long orderId);
}
