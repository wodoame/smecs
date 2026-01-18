package com.smecs.dao;

import com.smecs.model.Cart;
import com.smecs.model.CartItem;
import com.smecs.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Cart and CartItem operations.
 */
public class CartDAO {

    /**
     * Create a new cart for a user.
     */
    public Cart createCart(int userId) {
        String sql = "INSERT INTO Carts (user_id) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Cart cart = new Cart();
                        cart.setCartId(generatedKeys.getInt(1));
                        cart.setUserId(userId);
                        return cart;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating cart: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find a cart by user ID.
     */
    public Cart findCartByUserId(int userId) {
        String sql = "SELECT * FROM Carts WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Cart cart = mapResultSetToCart(rs);
                    cart.setItems(getCartItems(cart.getCartId()));
                    return cart;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding cart: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get or create a cart for a user.
     */
    public Cart getOrCreateCart(int userId) {
        Cart cart = findCartByUserId(userId);
        if (cart == null) {
            cart = createCart(userId);
        }
        return cart;
    }

    /**
     * Get all items in a cart.
     */
    public List<CartItem> getCartItems(int cartId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.*, p.product_name, c.category_name " +
                "FROM CartItems ci " +
                "JOIN Products p ON ci.product_id = p.product_id " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "WHERE ci.cart_id = ? " +
                "ORDER BY ci.added_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToCartItem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting cart items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Add an item to the cart. If the product already exists, update the quantity.
     */
    public CartItem addItemToCart(int cartId, int productId, int quantity, java.math.BigDecimal price) {
        // First check if the item already exists in the cart
        CartItem existingItem = findCartItem(cartId, productId);

        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + quantity;
            updateItemQuantity(existingItem.getCartItemId(), newQuantity);
            existingItem.setQuantity(newQuantity);
            updateCartTimestamp(cartId);
            return existingItem;
        }

        // Insert new item
        String sql = "INSERT INTO CartItems (cart_id, product_id, quantity, price_at_addition) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setBigDecimal(4, price);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        CartItem item = new CartItem();
                        item.setCartItemId(generatedKeys.getInt(1));
                        item.setCartId(cartId);
                        item.setProductId(productId);
                        item.setQuantity(quantity);
                        item.setPriceAtAddition(price);
                        updateCartTimestamp(cartId);
                        return item;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding item to cart: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Find a cart item by cart ID and product ID.
     */
    public CartItem findCartItem(int cartId, int productId) {
        String sql = "SELECT * FROM CartItems WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCartItemBasic(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding cart item: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update the quantity of a cart item.
     */
    public boolean updateItemQuantity(int cartItemId, int quantity) {
        String sql = "UPDATE CartItems SET quantity = ? WHERE cart_item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, cartItemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating cart item quantity: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove an item from the cart.
     */
    public boolean removeItemFromCart(int cartItemId) {
        String sql = "DELETE FROM CartItems WHERE cart_item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartItemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing item from cart: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Clear all items from a cart.
     */
    public boolean clearCart(int cartId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return clearCart(cartId, conn);
        } catch (SQLException e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clear all items from a cart using an existing connection.
     * Useful for transactions.
     */
    public boolean clearCart(int cartId, Connection conn) throws SQLException {
        String sql = "DELETE FROM CartItems WHERE cart_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            pstmt.executeUpdate();
            return true;
        }
    }

    /**
     * Update cart's updated_at timestamp.
     */
    private void updateCartTimestamp(int cartId) {
        String sql = "UPDATE Carts SET updated_at = CURRENT_TIMESTAMP WHERE cart_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating cart timestamp: " + e.getMessage());
        }
    }

    /**
     * Get the total number of items in a user's cart.
     */
    public int getCartItemCount(int userId) {
        String sql = "SELECT COALESCE(SUM(ci.quantity), 0) as total " +
                "FROM Carts c " +
                "JOIN CartItems ci ON c.cart_id = ci.cart_id " +
                "WHERE c.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting cart item count: " + e.getMessage());
        }
        return 0;
    }

    private Cart mapResultSetToCart(ResultSet rs) throws SQLException {
        Cart cart = new Cart();
        cart.setCartId(rs.getInt("cart_id"));
        cart.setUserId(rs.getInt("user_id"));
        cart.setCreatedAt(rs.getTimestamp("created_at"));
        cart.setUpdatedAt(rs.getTimestamp("updated_at"));
        return cart;
    }

    private CartItem mapResultSetToCartItem(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setCartItemId(rs.getInt("cart_item_id"));
        item.setCartId(rs.getInt("cart_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setPriceAtAddition(rs.getBigDecimal("price_at_addition"));
        item.setAddedAt(rs.getTimestamp("added_at"));
        item.setProductName(rs.getString("product_name"));
        item.setCategoryName(rs.getString("category_name"));
        return item;
    }

    private CartItem mapResultSetToCartItemBasic(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setCartItemId(rs.getInt("cart_item_id"));
        item.setCartId(rs.getInt("cart_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setPriceAtAddition(rs.getBigDecimal("price_at_addition"));
        item.setAddedAt(rs.getTimestamp("added_at"));
        return item;
    }
}

