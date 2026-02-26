package com.smecs.service.impl;

import com.smecs.config.CacheConfig;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    @CachePut(value = CacheConfig.ORDERS_BY_ID, key = "#result.id")
    @CacheEvict(value = {CacheConfig.ORDER_SEARCH, CacheConfig.USER_ORDER_SEARCH}, allEntries = true)
    public OrderDTO createOrder(CreateOrderRequestDTO request) {
        Long userId = request.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(0.0); // Placeholder, should be calculated
        order.setStatus(Order.Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        return toDTO(order);
    }

    @Override
    @Cacheable(value = CacheConfig.ORDERS_BY_ID, key = "#id")
    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id).map(this::toDTO).orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    @Transactional
    @Caching(put = {
            @CachePut(value = CacheConfig.ORDERS_BY_ID, key = "#result.id")
    }, evict = {
            @CacheEvict(value = CacheConfig.ORDER_SEARCH, allEntries = true),
            @CacheEvict(value = CacheConfig.USER_ORDER_SEARCH, allEntries = true)
    })
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
    @Cacheable(value = CacheConfig.ORDER_SEARCH, key = "T(com.smecs.service.impl.OrderServiceImpl).searchCacheKey(#query)")
    public PagedResponseDTO<OrderDTO> getAllOrders(OrderQuery query) {
        Pageable pageable = buildPageable(query);
        Order.Status status = Optional.ofNullable(query).map(OrderQuery::getStatus).orElse(null);
        Page<Order> orderPage = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return getPagedResponse(orderPage);
    }

    @Override
    @Cacheable(value = CacheConfig.USER_ORDER_SEARCH, key = "T(com.smecs.service.impl.OrderServiceImpl).userSearchCacheKey(#userId, #query)")
    public PagedResponseDTO<OrderDTO> getOrdersByUserId(Long userId, OrderQuery query) {
        Page<Order> orderPage = orderRepository.findByUser_Id(userId, buildPageable(query));
        return getPagedResponse(orderPage);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ORDERS_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.ORDER_SEARCH, allEntries = true),
            @CacheEvict(value = CacheConfig.USER_ORDER_SEARCH, allEntries = true)
    })
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
             throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ORDERS_BY_ID, key = "#orderId"),
            @CacheEvict(value = CacheConfig.ORDER_SEARCH, allEntries = true),
            @CacheEvict(value = CacheConfig.USER_ORDER_SEARCH, allEntries = true)
    })
    @Transactional
    public void updateOrderTotalOrThrow(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
        double total = items.stream()
                .mapToDouble(item -> item.getPriceAtPurchase() * item.getQuantity())
                .sum();

        order.setTotalAmount(total);
        orderRepository.save(order);
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
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

    public static String searchCacheKey(OrderQuery query) {
        OrderQuery normalized = query != null ? query : OrderQuery.builder().build();
        int page = normalized.getPage() != null ? normalized.getPage() : 1;
        int size = normalized.getSize() != null ? normalized.getSize() : 10;
        String sort = normalized.getSort() != null ? normalized.getSort() : "createdAt,desc";
        String statusKey = normalized.getStatus() != null ? normalized.getStatus().name() : "";

        return String.format("status:%s|page:%d|size:%d|sort:%s", statusKey, page, size, sort);
    }

    public static String userSearchCacheKey(Long userId, OrderQuery query) {
        Long normalizedUserId = userId != null ? userId : -1L;
        return String.format("user:%d|%s", normalizedUserId, searchCacheKey(query));
    }
}
