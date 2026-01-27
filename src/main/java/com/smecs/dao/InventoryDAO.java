package com.smecs.dao;

import com.smecs.model.Inventory;
import com.smecs.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public void addInventory(Inventory inventory) {
        String sql = "INSERT INTO Inventory (product_id, quantity) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, inventory.getProductId());
            pstmt.setInt(2, inventory.getQuantity());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding inventory: " + e.getMessage());
        }
    }

    public void updateInventory(Inventory inventory) {
        String sql = "UPDATE Inventory SET quantity = ? WHERE inventory_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, inventory.getQuantity());
            pstmt.setInt(2, inventory.getInventoryId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating inventory: " + e.getMessage());
        }
    }

    public void updateQuantityByProductId(int productId, int quantity) {
        String sql = "UPDATE Inventory SET quantity = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating inventory by product ID: " + e.getMessage());
        }
    }

    public void deleteInventory(int inventoryId) {
        String sql = "DELETE FROM Inventory WHERE inventory_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, inventoryId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting inventory: " + e.getMessage());
        }
    }

    public List<Inventory> findAll() {
        List<Inventory> inventoryList = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, i.inventory_id, COALESCE(i.quantity, 0) as quantity " +
                     "FROM Products p " +
                     "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                     "ORDER BY p.product_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                inventoryList.add(mapResultSetToInventory(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching inventory: " + e.getMessage());
        }
        return inventoryList;
    }

    public Inventory findByProductId(int productId) {
        String sql = "SELECT i.*, p.product_name FROM Inventory i " +
                     "LEFT JOIN Products p ON i.product_id = p.product_id " +
                     "WHERE i.product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInventory(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding inventory by product ID: " + e.getMessage());
        }
        return null;
    }

    public boolean inventoryExistsForProduct(int productId) {
        String sql = "SELECT COUNT(*) FROM Inventory WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking inventory existence: " + e.getMessage());
        }
        return false;
    }

    public void reduceStock(int productId, int quantity, Connection conn) throws SQLException {
        String sql = "UPDATE Inventory SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Insufficient stock or product not found in inventory for product ID: " + productId);
            }
        }
    }

    private Inventory mapResultSetToInventory(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setInventoryId(rs.getInt("inventory_id"));
        inventory.setProductId(rs.getInt("product_id"));
        inventory.setQuantity(rs.getInt("quantity"));
        inventory.setProductName(rs.getString("product_name"));
        return inventory;
    }
}
