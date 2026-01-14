package com.smecs.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shopping cart for a user.
 */
public class Cart {
    private int cartId;
    private int userId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<CartItem> items;

    public Cart() {
        this.items = new ArrayList<>();
    }

    public Cart(int cartId, int userId, Timestamp createdAt, Timestamp updatedAt) {
        this.cartId = cartId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = new ArrayList<>();
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void addItem(CartItem item) {
        this.items.add(item);
    }

    public void removeItem(CartItem item) {
        this.items.remove(item);
    }

    /**
     * Calculate total price of all items in the cart.
     */
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get total number of items in the cart.
     */
    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}

