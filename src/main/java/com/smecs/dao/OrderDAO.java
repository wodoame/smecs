package com.smecs.dao;

import com.smecs.model.Order;
import com.smecs.model.OrderItem;

import java.sql.*;

public class OrderDAO {

    public Order createOrder(Order order, Connection conn) throws SQLException {
        String sql = "INSERT INTO Orders (user_id, total_amount, status) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, order.getUserId());
            pstmt.setBigDecimal(2, order.getTotalAmount());
            pstmt.setString(3, order.getStatus());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderId(rs.getInt(1));
                        return order;
                    }
                }
            }
        }
        return null;
    }

    public void addOrderItem(OrderItem item, Connection conn) throws SQLException {
        String sql = "INSERT INTO OrderItems (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.getOrderId());
            pstmt.setInt(2, item.getProductId());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setBigDecimal(4, item.getPrice());
            pstmt.executeUpdate();
        }
    }
}
