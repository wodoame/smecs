package com.smecs.dao;

import com.smecs.entity.Order;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    boolean existsById(Long id);
    void deleteById(Long id);
}

