package com.smecs.service.impl;

import com.smecs.entity.Cart;
import com.smecs.entity.User;
import com.smecs.repository.CartRepository;
import com.smecs.repository.UserRepository;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    @Override
    public Optional<Cart> getCartById(Long cartId) {
        return cartRepository.findById(cartId);
    }

    @Override
    public Cart createCart(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        Cart existingCart = cartRepository.findByUserId(userId);
        if (existingCart != null) {
            return existingCart;
        }
        Cart cart = new Cart();
        cart.setUser(userOpt.get());
        cart.setCreatedAt(java.time.LocalDateTime.now());
        cart.setUpdatedAt(java.time.LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Override
    public void deleteCart(Long cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new ResourceNotFoundException("Cart not found with id: " + cartId);
        }
        cartRepository.deleteById(cartId);
    }
}
