package com.smecs.test;

import com.smecs.dao.CategoryDAO;
import com.smecs.dao.ProductDAO;
import com.smecs.model.Category;
import com.smecs.model.Product;
import com.smecs.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class VerificationTest {

    public static void main(String[] args) {
        System.out.println("Starting Verification...");

        // 1. Test Connection
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("[PASS] Database Connection Successful");
        } catch (Exception e) {
            System.err.println("[FAIL] Database Connection Failed");
            e.printStackTrace();
            return;
        }

        // Ensure a category exists for testing
        int testCategoryId = getOrCreateTestCategory();
        if (testCategoryId == -1) {
            System.err.println("[FAIL] Could not get or create a test category.");
            return;
        }

        // 2. Test ProductDAO CRUD
        ProductDAO productDAO = new ProductDAO();
        Product testProduct = new Product();
        testProduct.setProductName("Test Widget");
        testProduct.setCategoryId(testCategoryId);
        testProduct.setPrice(new BigDecimal("19.99"));
        testProduct.setDescription("A widget for testing.");

        // Create
        System.out.println("Testing Create...");
        try {
            productDAO.addProduct(testProduct);
            System.out.println("[PASS] Product Inserted (no exception)");
        } catch (Exception e) {
            System.err.println("[FAIL] Product Insert Failed");
            e.printStackTrace();
        }

        // Read/Search
        System.out.println("Testing Read/Search...");
        List<Product> products = productDAO.searchProducts("Test Widget");
        if (!products.isEmpty()) {
            System.out.println("[PASS] Product Found: " + products.get(0).getProductName());
            testProduct = products.get(0); // Update reference with ID
        } else {
            System.err.println("[FAIL] Product Not Found after Insertion");
        }

        // Update
        System.out.println("Testing Update...");
        testProduct.setPrice(new BigDecimal("29.99"));
        try {
            productDAO.updateProduct(testProduct);
            System.out.println("[PASS] Product Updated (no exception)");
        } catch (Exception e) {
            System.err.println("[FAIL] Product Update Failed");
            e.printStackTrace();
        }

        // Verify Update
        products = productDAO.searchProducts("Test Widget");
        if (!products.isEmpty() && products.get(0).getPrice().compareTo(new BigDecimal("29.99")) == 0) {
            System.out.println("[PASS] Product Price verified as 29.99");
        } else {
            System.err.println("[FAIL] Product Update Verification Failed");
        }

        // Delete
        System.out.println("Testing Delete...");
        try {
            productDAO.deleteProduct(testProduct.getProductId());
            System.out.println("[PASS] Product Deleted (no exception)");
        } catch (Exception e) {
            System.err.println("[FAIL] Product Delete Failed");
            e.printStackTrace();
        }

        // Verify Delete
        products = productDAO.searchProducts("Test Widget");
        if (products.isEmpty()) {
            System.out.println("[PASS] Product successfully removed from search results.");
        } else {
            // Note: search uses LIKE %..%, so make sure we don't match other things if DB
            // is populated.
            // But assuming unique name for test helps.
            System.out.println("[INFO] Search returned " + products.size() + " items. Ideally 0 if DB was empty.");
        }

        System.out.println("Verification Complete.");
    }

    private static int getOrCreateTestCategory() {
        // Try to find one
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT category_id FROM Categories LIMIT 1");
            if (rs.next()) {
                return rs.getInt(1);
            }

            // Create one if none exists
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO Categories (category_name, description) VALUES (?, ?) RETURNING category_id");
            pstmt.setString(1, "Test Category");
            pstmt.setString(2, "Category for automated testing");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
