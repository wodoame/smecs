package com.smecs.service;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.UpdateOrderStatusRequestDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface OrderService {
    OrderDTO createOrder(CreateOrderRequestDTO request);
    OrderDTO getOrderById(Long id);
    OrderDTO updateOrderStatus(Long id, UpdateOrderStatusRequestDTO request);
    List<OrderDTO> getAllOrders();
    PagedResponseDTO<OrderDTO> getAllOrders(Pageable pageable);
    List<OrderDTO> getOrdersByUserId(Long userId);
    PagedResponseDTO<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable);
    void deleteOrder(Long id);
    void updateOrderTotalOrThrow(Long orderId);
}
