package com.smecs.service.impl;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.entity.Order;
import com.smecs.entity.User;
import com.smecs.repository.OrderRepository;
import com.smecs.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements com.smecs.service.OrderService {
    private final OrderRepository orderRepository;
    private final UserDAO userDAO;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserDAO userDAO) {
        this.orderRepository = orderRepository;
        this.userDAO = userDAO;
    }

    @Override
    public OrderDTO createOrder(CreateOrderRequestDTO request) {
        Optional<User> userOpt = userDAO.findById(request.getUserId());
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");
        Order order = new Order();
        order.setUser(userOpt.get());
        order.setTotalAmount(0.0); // Placeholder, should be calculated
        order.setStatus(Order.Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        return toDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().toString());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}
