package com.smecs.service;

import com.smecs.cache.CategoryCache;
import com.smecs.dao.CategoryDAO;
import com.smecs.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Category operations with caching support.
 * Implements in-memory caching for faster retrieval as per Epic 3.
 */
public class CategoryService {
    private final CategoryDAO categoryDAO;
    private final CategoryCache cache;
    private final boolean cachingEnabled = true;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.cache = CategoryCache.getInstance();
    }

    /**
     * Get all categories with caching support.
     */
    public List<Category> getAllCategories() {
        if (cachingEnabled) {
            Optional<List<Category>> cached = cache.getAllCategories();
            if (cached.isPresent()) {
                System.out.println("[Cache] HIT: getAllCategories");
                return cached.get();
            }
            System.out.println("[Cache] MISS: getAllCategories - fetching from database");
        }

        List<Category> categories = categoryDAO.findAll();

        if (cachingEnabled) {
            cache.putAllCategories(categories);
        }

        return categories;
    }

    /**
     * Create a new category with cache invalidation.
     */
    public boolean createCategory(Category category) {
        boolean result = categoryDAO.createCategory(category);
        if (result && cachingEnabled) {
            cache.invalidateAll();
            System.out.println("[Cache] Invalidated after category creation");
        }
        return result;
    }

    /**
     * Update a category with cache invalidation.
     */
    public boolean updateCategory(Category category) {
        boolean result = categoryDAO.updateCategory(category);
        if (result && cachingEnabled) {
            cache.invalidateCategory(category.getCategoryId());
            System.out.println("[Cache] Invalidated category " + category.getCategoryId());
        }
        return result;
    }

    /**
     * Delete a category with cache invalidation.
     */
    public boolean deleteCategory(int categoryId) {
        boolean result = categoryDAO.deleteCategory(categoryId);
        if (result && cachingEnabled) {
            cache.invalidateCategory(categoryId);
            System.out.println("[Cache] Invalidated category " + categoryId);
        }
        return result;
    }

}
