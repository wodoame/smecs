package com.smecs.dao;

import com.smecs.entity.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartItemDAO {
    CartItem save(CartItem cartItem);
    Optional<CartItem> findById(Long id);
    List<CartItem> findByCartId(Long cartId);
    CartItem findByCartIdAndProductId(Long cartId, Long productId);
    boolean existsById(Long id);
    void deleteById(Long id);
    void deleteByCartIdAndProductId(Long cartId, Long productId);
}

