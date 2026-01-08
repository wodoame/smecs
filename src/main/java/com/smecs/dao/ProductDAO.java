package com.smecs.dao;

import com.smecs.model.Product;
import com.smecs.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public void addProduct(Product product) {
        String sql = "INSERT INTO Products (category_id, product_name, description, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, product.getCategoryId());
            pstmt.setString(2, product.getProductName());
            pstmt.setString(3, product.getDescription());
            pstmt.setBigDecimal(4, product.getPrice());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateProduct(Product product) {
        String sql = "UPDATE Products SET category_id = ?, product_name = ?, description = ?, price = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, product.getCategoryId());
            pstmt.setString(2, product.getProductName());
            pstmt.setString(3, product.getDescription());
            pstmt.setBigDecimal(4, product.getPrice());
            pstmt.setInt(5, product.getProductId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteProduct(int productId) {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name FROM Products p LEFT JOIN Categories c ON p.category_id = c.category_id ORDER BY p.product_id";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        // Search by product name or category name
        String sql = "SELECT p.*, c.category_name FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "WHERE LOWER(p.product_name) LIKE ? OR LOWER(c.category_name) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + query.toLowerCase() + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setProductName(rs.getString("product_name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setCategoryName(rs.getString("category_name"));
        return product;
    }
}
