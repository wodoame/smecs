package com.smecs.dao;

import com.smecs.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    Page<Order> findAll(Pageable pageable);
    List<Order> findByUserId(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    boolean existsById(Long id);
    void deleteById(Long id);
}

