package com.smecs.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Represents an item in a shopping cart.
 */
public class CartItem {
    private int cartItemId;
    private int cartId;
    private int productId;
    private int quantity;
    private BigDecimal priceAtAddition;
    private Timestamp addedAt;

    // For UI display convenience
    private String productName;
    private String categoryName;

    public CartItem() {
    }

    public CartItem(int cartItemId, int cartId, int productId, int quantity, BigDecimal priceAtAddition, Timestamp addedAt) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtAddition = priceAtAddition;
        this.addedAt = addedAt;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtAddition() {
        return priceAtAddition;
    }

    public void setPriceAtAddition(BigDecimal priceAtAddition) {
        this.priceAtAddition = priceAtAddition;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Timestamp addedAt) {
        this.addedAt = addedAt;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Calculate subtotal for this cart item.
     */
    public BigDecimal getSubtotal() {
        if (priceAtAddition == null) {
            return BigDecimal.ZERO;
        }
        return priceAtAddition.multiply(BigDecimal.valueOf(quantity));
    }
}

