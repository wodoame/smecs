package com.smecs.service;

import com.smecs.entity.Cart;
import com.smecs.entity.CartItem;
import com.smecs.entity.Product;
import com.smecs.entity.User;
import com.smecs.repository.CartItemRepository;
import com.smecs.repository.CartRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public CartItem addToCart(Long userId, Long productId, int quantity) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Product> productOpt = productRepository.findById(productId);
        if (userOpt.isEmpty() || productOpt.isEmpty()) {
            throw new IllegalArgumentException("User or Product not found");
        }
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(userOpt.get());
            cart = cartRepository.save(cart);
        }
        Product product = productOpt.get();
        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return cartItemRepository.save(item);
            }
        }
        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(product);
        newItem.setQuantity(quantity);
        newItem.setPriceAtAddition(product.getPrice());
        return cartItemRepository.save(newItem);
    }

    public Cart getCurrentUserCart(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public List<CartItem> getCartItems(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) return Collections.emptyList();
        return cartItemRepository.findByCartId(cart.getCartId());
    }

    public boolean updateItemQuantity(Long userId, Long cartItemId, int quantity) {
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isEmpty()) return false;
        CartItem item = itemOpt.get();
        if (!item.getCart().getUser().getId().equals(userId)) return false;
        if (quantity <= 0) {
            cartItemRepository.deleteById(cartItemId);
            return true;
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return true;
    }

    public boolean removeFromCart(Long userId, Long cartItemId) {
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isEmpty()) return false;
        CartItem item = itemOpt.get();
        if (!item.getCart().getUser().getId().equals(userId)) return false;
        cartItemRepository.deleteById(cartItemId);
        return true;
    }

    public boolean clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) return false;
        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());
        cartItemRepository.deleteAll(items);
        return true;
    }

    public int getCartItemCount(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) return 0;
        return cartItemRepository.findByCartId(cart.getCartId()).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isProductInCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) return false;
        return cartItemRepository.findByCartId(cart.getCartId()).stream().anyMatch(item -> item.getProduct().getId().equals(productId));
    }
}
