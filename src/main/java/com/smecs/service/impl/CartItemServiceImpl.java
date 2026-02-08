package com.smecs.service.impl;

import com.smecs.dto.AddToCartRequest;
import com.smecs.entity.Cart;
import com.smecs.entity.CartItem;
import com.smecs.entity.Product;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.dao.CartItemDAO;
import com.smecs.dao.ProductDAO;
import com.smecs.service.CartItemService;
import com.smecs.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService {
    private final CartItemDAO cartItemDAO;
    private final CartService cartService;
    private final ProductDAO productDAO;

    @Autowired
    public CartItemServiceImpl(CartItemDAO cartItemDAO, CartService cartService, ProductDAO productDAO) {
        this.cartItemDAO = cartItemDAO;
        this.cartService = cartService;
        this.productDAO = productDAO;
    }

    @Override
    public CartItem addItemToCart(AddToCartRequest request) {
        Cart cart = cartService.getCartById(request.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + request.getCartId()));
        Product product = productDAO.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        CartItem existingItem = cartItemDAO.findByCartIdAndProductId(cart.getCartId(), product.getId());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            return cartItemDAO.save(existingItem);
        }

        CartItem newItem = new CartItem();
        newItem.setCartId(cart.getCartId());
        newItem.setProduct(product);
        newItem.setQuantity(request.getQuantity());
        newItem.setAddedAt(LocalDateTime.now());

        return cartItemDAO.save(newItem);
    }

    @Override
    public CartItem saveCartItem(CartItem cartItem) {
        return cartItemDAO.save(cartItem);
    }

    @Override
    public CartItem updateCartItem(Long id, int quantity) {
        CartItem item = getCartItemById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item not found with id " + id));
        item.setQuantity(quantity);
        return cartItemDAO.save(item);
    }

    @Override
    public java.util.Optional<CartItem> getCartItemById(Long cartItemId) {
        return cartItemDAO.findById(cartItemId);
    }

    @Override
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        return cartItemDAO.findByCartId(cartId);
    }

    @Override
    public void deleteCartItem(Long cartItemId) {
        if (!cartItemDAO.existsById(cartItemId)) {
            throw new ResourceNotFoundException("CartItem not found with id: " + cartItemId);
        }
        cartItemDAO.deleteById(cartItemId);
    }
}
