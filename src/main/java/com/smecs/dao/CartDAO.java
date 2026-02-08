package com.smecs.dao;

import com.smecs.entity.Cart;
import java.util.List;
import java.util.Optional;

public interface CartDAO {
    Cart save(Cart cart);
    Optional<Cart> findById(Long id);
    List<Cart> findAll();
    Cart findByUserId(Long userId);
    boolean existsById(Long id);
    void deleteById(Long id);
}

