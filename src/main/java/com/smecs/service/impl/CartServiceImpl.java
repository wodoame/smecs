package com.smecs.service.impl;

import com.smecs.entity.Cart;
import com.smecs.entity.User;
import com.smecs.entity.CartItem;
import com.smecs.repository.CartRepository;
import com.smecs.repository.UserRepository;
import com.smecs.repository.CartItemRepository;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.service.CartService;
import com.smecs.security.OwnershipChecks;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final OwnershipChecks ownershipChecks;

    @Override
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    @Override
    public Optional<Cart> getCartById(Long cartId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        cartOpt.ifPresent(ownershipChecks::assertCartOwnership);
        return cartOpt;
    }

    @Override
    public Cart createCart(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        Cart existingCart = cartRepository.findById(userId).orElse(null);
        if (existingCart != null) {
            return existingCart;
        }
        return createNewCart(userOpt.get());
    }

    @Override
    @Transactional
    public Cart getOrCreateCartForUser(Long userId) {
        return cartRepository.findByCartId(userId)
                .orElseGet(() -> createCartWithRetry(userId));
    }

    private Cart createCartWithRetry(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        try {
            return createNewCart(user);
        } catch (DataIntegrityViolationException exception) {
            return cartRepository.findByCartId(userId)
                    .orElseThrow(() -> exception);
        }
    }

    private Cart createNewCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(java.time.LocalDateTime.now());
        cart.setUpdatedAt(java.time.LocalDateTime.now());
        return cartRepository.saveAndFlush(cart);
    }

    @Override
    public void deleteCart(Long cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new ResourceNotFoundException("Cart not found with id: " + cartId);
        }
        cartRepository.deleteById(cartId);
    }

    @Override
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        ownershipChecks.assertCartOwnership(cart);

        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        if (items != null && !items.isEmpty()) {
            cartItemRepository.deleteAll(items);
        }

        cart.setUpdatedAt(java.time.LocalDateTime.now());
        cartRepository.save(cart);
    }
}
