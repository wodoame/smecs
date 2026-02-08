package com.smecs.service;

import com.smecs.dto.CartItemRequest;
import com.smecs.entity.Cart;
import com.smecs.entity.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartService {
    List<Cart> getAllCarts();
    Optional<Cart> getCartById(Long cartId);
    Cart createCart(Long userId);
    void deleteCart(Long cartId);
}

