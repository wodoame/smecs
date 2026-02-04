package com.smecs.service;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import java.util.List;

public interface OrderService {
    OrderDTO createOrder(CreateOrderRequestDTO request);
    OrderDTO getOrderById(Long id);
    List<OrderDTO> getAllOrders();
    void deleteOrder(Long id);
}
