package com.smecs.service;

import com.smecs.dao.CartDAO;
import com.smecs.dao.InventoryDAO;
import com.smecs.dao.OrderDAO;
import com.smecs.model.Cart;
import com.smecs.model.CartItem;
import com.smecs.model.Order;
import com.smecs.model.OrderItem;
import com.smecs.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OrderService {
    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final InventoryDAO inventoryDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.cartDAO = new CartDAO();
        this.inventoryDAO = new InventoryDAO();
    }

    public Order placeOrder(int userId) {
        Cart cart = cartDAO.getOrCreateCart(userId);
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        List<CartItem> cartItems = cartDAO.getCartItems(cart.getCartId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalAmount = cartItems.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Create Order
            Order createdOrder = orderDAO.createOrder(order, conn);
            if (createdOrder == null) {
                throw new SQLException("Failed to create order");
            }

            // 2. Create Order Items
            for (CartItem ci : cartItems) {
                OrderItem oi = new OrderItem();
                oi.setOrderId(createdOrder.getOrderId());
                oi.setProductId(ci.getProductId());
                oi.setQuantity(ci.getQuantity());
                oi.setPrice(ci.getPriceAtAddition());

                orderDAO.addOrderItem(oi, conn);

                // Deduct stock from inventory
                inventoryDAO.reduceStock(ci.getProductId(), ci.getQuantity(), conn);
            }

            // 3. Clear Cart
            cartDAO.clearCart(cart.getCartId(), conn);

            conn.commit();
            return createdOrder;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Failed to place order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
