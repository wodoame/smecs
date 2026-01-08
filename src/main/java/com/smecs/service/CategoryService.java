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
}
