package com.smecs.dao;

import com.smecs.model.Category;
import com.smecs.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories ORDER BY category_name";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setCategoryName(rs.getString("category_name"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // In a real app, we might throw a custom exception or log this better
        }
        return categories;
    }
}
