package com.smecs.dao;

import com.smecs.entity.OrderItem;
import java.util.List;
import java.util.Optional;

public interface OrderItemDAO {
    OrderItem save(OrderItem orderItem);
    List<OrderItem> saveAll(List<OrderItem> orderItems);
    Optional<OrderItem> findById(Long id);
    List<OrderItem> findByOrderId(Long orderId);
    boolean existsById(Long id);
    void deleteById(Long id);
}

