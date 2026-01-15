package com.smecs.service;

import com.smecs.dao.CartDAO;
import com.smecs.model.Cart;
import com.smecs.model.CartItem;
import com.smecs.model.Product;
import com.smecs.util.SessionManager;

import java.util.List;

/**
 * Service class for cart operations.
 * Handles business logic related to shopping cart functionality.
 */
public class CartService {
    private final CartDAO cartDAO;

    public CartService() {
        this.cartDAO = new CartDAO();
    }

    /**
     * Add a product to the current user's cart.
     * 
     * @param product  The product to add
     * @param quantity The quantity to add
     * @return The cart item that was added/updated, or null if failed
     * @throws IllegalStateException if user is not logged in
     */
    public CartItem addToCart(Product product, int quantity) {
        SessionManager session = SessionManager.getInstance();

        if (!session.isLoggedIn()) {
            throw new IllegalStateException("User must be logged in to add items to cart");
        }

        int userId = session.getCurrentUserId();
        Cart cart = cartDAO.getOrCreateCart(userId);

        if (cart == null) {
            throw new RuntimeException("Failed to get or create cart for user");
        }

        return cartDAO.addItemToCart(cart.getCartId(), product.getProductId(), quantity, product.getPrice());
    }

    /**
     * Get the current user's cart.
     * 
     * @return The user's cart, or null if not logged in
     */
    public Cart getCurrentUserCart() {
        SessionManager session = SessionManager.getInstance();

        if (!session.isLoggedIn()) {
            return null;
        }

        return cartDAO.getOrCreateCart(session.getCurrentUserId());
    }

    /**
     * Get all items in the current user's cart.
     * 
     * @return List of cart items, or empty list if not logged in
     */
    public List<CartItem> getCartItems() {
        Cart cart = getCurrentUserCart();
        if (cart == null) {
            return List.of();
        }
        return cartDAO.getCartItems(cart.getCartId());
    }

    /**
     * Update the quantity of an item in the cart.
     * 
     * @param cartItemId The cart item ID
     * @param quantity   The new quantity
     * @return true if successful
     */
    public boolean updateItemQuantity(int cartItemId, int quantity) {
        if (quantity <= 0) {
            return removeFromCart(cartItemId);
        }
        return cartDAO.updateItemQuantity(cartItemId, quantity);
    }

    /**
     * Remove an item from the cart.
     * 
     * @param cartItemId The cart item ID to remove
     * @return true if successful
     */
    public boolean removeFromCart(int cartItemId) {
        return cartDAO.removeItemFromCart(cartItemId);
    }

    /**
     * Clear all items from the current user's cart.
     * 
     * @return true if successful
     */
    public boolean clearCart() {
        Cart cart = getCurrentUserCart();
        if (cart == null) {
            return false;
        }
        return cartDAO.clearCart(cart.getCartId());
    }

    /**
     * Get the total number of items in the current user's cart.
     * 
     * @return The total item count, or 0 if not logged in
     */
    public int getCartItemCount() {
        SessionManager session = SessionManager.getInstance();

        if (!session.isLoggedIn()) {
            return 0;
        }

        return cartDAO.getCartItemCount(session.getCurrentUserId());
    }

    /**
     * Check if a product is in the current user's cart.
     * 
     * @param productId The product ID to check
     * @return true if the product is in the cart
     */
    public boolean isProductInCart(int productId) {
        Cart cart = getCurrentUserCart();
        if (cart == null) {
            return false;
        }
        return cartDAO.findCartItem(cart.getCartId(), productId) != null;
    }
}

