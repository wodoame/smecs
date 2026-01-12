package com.smecs.service;

import com.smecs.dao.CategoryDAO;
import com.smecs.model.Category;

import java.util.List;

public class CategoryService {
    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public boolean createCategory(Category category) {
        return categoryDAO.createCategory(category);
    }

    public boolean updateCategory(Category category) {
        return categoryDAO.updateCategory(category);
    }

    public boolean deleteCategory(int categoryId) {
        return categoryDAO.deleteCategory(categoryId);
    }

    public boolean categoryNameExists(String categoryName) {
        return categoryDAO.categoryNameExists(categoryName);
    }
}
