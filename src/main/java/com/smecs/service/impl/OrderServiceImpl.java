package com.smecs.service.impl;

import com.smecs.dto.CreateOrderRequestDTO;
import com.smecs.dto.OrderDTO;
import com.smecs.dto.OrderQuery;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.UpdateOrderStatusRequestDTO;
import com.smecs.entity.Order;
import com.smecs.entity.OrderItem;
import com.smecs.entity.User;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.OrderRepository;
import com.smecs.repository.UserRepository;
import com.smecs.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.smecs.service.OrderService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public OrderDTO createOrder(CreateOrderRequestDTO request) {
        Long userId = request.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Order order = new Order();
        order.setUserId(user.getId());
        order.setTotalAmount(0.0); // Placeholder, should be calculated
        order.setStatus(Order.Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        return toDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id).map(this::toDTO).orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, UpdateOrderStatusRequestDTO request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        try {
            Order.Status status = Order.Status.valueOf(request.getStatus().toUpperCase());
            order.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + request.getStatus());
        }

        return toDTO(orderRepository.save(order));
    }

    @Override
    public PagedResponseDTO<OrderDTO> getAllOrders(OrderQuery query) {
        Page<Order> orderPage = orderRepository.findAll(buildPageable(query));
        return getPagedResponse(orderPage);
    }

    @Override
    public PagedResponseDTO<OrderDTO> getOrdersByUserId(Long userId, OrderQuery query) {
        Page<Order> orderPage = orderRepository.findByUserId(userId, buildPageable(query));
        return getPagedResponse(orderPage);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
             throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }

    @Override
    public void updateOrderTotalOrThrow(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        double total = items.stream()
                .mapToDouble(item -> item.getPriceAtPurchase() * item.getQuantity())
                .sum();

        order.setTotalAmount(total);
        orderRepository.save(order);
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

    private PagedResponseDTO<OrderDTO> getPagedResponse(Page<Order> orderPage) {
        List<OrderDTO> content = orderPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PagedResponseDTO<OrderDTO> pagedResponse = new PagedResponseDTO<>();
        pagedResponse.setContent(content);
        pagedResponse.setPage(PageMetadataDTO.from(orderPage));

        return pagedResponse;
    }

    private Pageable buildPageable(OrderQuery query) {
        int pageIndex = Math.max(0, Optional.ofNullable(query).map(OrderQuery::getPage).orElse(1) - 1);
        int pageSize = Math.max(1, Optional.ofNullable(query).map(OrderQuery::getSize).orElse(10));
        String sortClause = Optional.ofNullable(query).map(OrderQuery::getSort).orElse("createdAt,desc");

        String sortField = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (!sortClause.isBlank()) {
            String[] sortParams = sortClause.split(",");
            if (sortParams.length > 0 && !sortParams[0].isBlank()) {
                sortField = sortParams[0];
            }
            if (sortParams.length > 1 && !sortParams[1].isBlank()) {
                direction = sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            }
        }

        return PageRequest.of(pageIndex, pageSize, Sort.by(direction, sortField));
    }
}
