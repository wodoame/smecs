package com.smecs.service.impl;

import com.smecs.entity.Cart;
import com.smecs.entity.User;
import com.smecs.dao.CartDAO;
import com.smecs.dao.UserDAO;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    private final CartDAO cartDAO;
    private final UserDAO userDAO;

    @Autowired
    public CartServiceImpl(CartDAO cartDAO, UserDAO userDAO) {
        this.cartDAO = cartDAO;
        this.userDAO = userDAO;
    }

    @Override
    public List<Cart> getAllCarts() {
        return cartDAO.findAll();
    }

    @Override
    public Optional<Cart> getCartById(Long cartId) {
        return cartDAO.findById(cartId);
    }

    @Override
    public Cart createCart(Long userId) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        Cart existingCart = cartDAO.findByUserId(userId);
        if (existingCart != null) {
            return existingCart;
        }
        Cart cart = new Cart();
        cart.setUser(userOpt.get());
        cart.setCreatedAt(java.time.LocalDateTime.now());
        cart.setUpdatedAt(java.time.LocalDateTime.now());
        return cartDAO.save(cart);
    }

    @Override
    public void deleteCart(Long cartId) {
        if (!cartDAO.existsById(cartId)) {
            throw new ResourceNotFoundException("Cart not found with id: " + cartId);
        }
        cartDAO.deleteById(cartId);
    }
}
