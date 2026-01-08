package com.smecs.service;

import com.smecs.dao.ProductDAO;
import com.smecs.model.Product;

import java.util.List;

public class ProductService {
    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        return productDAO.searchProducts(query);
    }

    public void createProduct(Product product) {
        // Here we could add validation logic
        if (product.getPrice().doubleValue() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        productDAO.addProduct(product);
    }

    public void updateProduct(Product product) {
        if (product.getPrice().doubleValue() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        productDAO.updateProduct(product);
    }

    public void deleteProduct(int productId) {
        productDAO.deleteProduct(productId);
    }
}
