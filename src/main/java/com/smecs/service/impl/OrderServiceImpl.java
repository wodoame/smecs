package com.smecs.service.impl;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.dto.UpdateOrderStatusRequestDTO;
import com.smecs.entity.Order;
import com.smecs.entity.User;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.dao.OrderDAO;
import com.smecs.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.smecs.service.OrderService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderDAO orderDAO;
    private final UserDAO userDAO;

    @Autowired
    public OrderServiceImpl(OrderDAO orderDAO, UserDAO userDAO) {
        this.orderDAO = orderDAO;
        this.userDAO = userDAO;
    }

    @Override
    public OrderDTO createOrder(CreateOrderRequestDTO request) {
        Long userId = request.getUserId();
        Optional<User> user = Optional.of(userDAO.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + userId)));
        Order order = new Order();
        order.setUserId(user.get().getId());
        order.setTotalAmount(0.0); // Placeholder, should be calculated
        order.setStatus(Order.Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order = orderDAO.save(order);
        return toDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        return orderDAO.findById(id).map(this::toDTO).orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, UpdateOrderStatusRequestDTO request) {
        Order order = orderDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        try {
            Order.Status status = Order.Status.valueOf(request.getStatus().toUpperCase());
            order.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + request.getStatus());
        }

        return toDTO(orderDAO.save(order));
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderDAO.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        orderDAO.deleteById(id);
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().toString());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}
