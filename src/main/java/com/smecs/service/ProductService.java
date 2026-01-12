package com.smecs.service;

import com.smecs.cache.ProductCache;
import com.smecs.dao.ProductDAO;
import com.smecs.model.Product;
import com.smecs.util.ProductSearcher;
import com.smecs.util.ProductSorter;
import com.smecs.util.ProductSorter.SortCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Product operations with caching and sorting support.
 * Implements in-memory caching for faster retrieval as per Epic 3.
 */
public class ProductService {
    private final ProductDAO productDAO;
    private final ProductCache cache;
    private final ProductSearcher searcher;

    // Flag to enable/disable caching (useful for performance comparison)
    private boolean cachingEnabled = true;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.cache = ProductCache.getInstance();
        this.searcher = new ProductSearcher();
    }

    /**
     * Get all products with caching support.
     */
    public List<Product> getAllProducts() {
        if (cachingEnabled) {
            Optional<List<Product>> cached = cache.getAllProducts();
            if (cached.isPresent()) {
                System.out.println("[Cache] HIT: getAllProducts");
                return cached.get();
            }
            System.out.println("[Cache] MISS: getAllProducts - fetching from database");
        }

        List<Product> products = productDAO.findAll();

        if (cachingEnabled) {
            cache.putAllProducts(products);
            searcher.buildIndex(products);
        }

        return products;
    }

    /**
     * Get all products with sorting applied.
     */
    public List<Product> getAllProductsSorted(SortCriteria sortCriteria) {
        List<Product> products = new ArrayList<>(getAllProducts());
        ProductSorter.sort(products, sortCriteria);
        return products;
    }

    /**
     * Search products by name or category with caching support.
     * Implements case-insensitive search optimized through caching and hashing.
     */
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }

        String normalizedQuery = query.trim();

        if (cachingEnabled) {
            // Try cache first
            Optional<List<Product>> cached = cache.getSearchResults(normalizedQuery);
            if (cached.isPresent()) {
                System.out.println("[Cache] HIT: search for '" + normalizedQuery + "'");
                return cached.get();
            }
            System.out.println("[Cache] MISS: search for '" + normalizedQuery + "' - querying database");
        }

        // Fetch from database
        List<Product> results = productDAO.searchProducts(normalizedQuery);

        if (cachingEnabled) {
            cache.putSearchResults(normalizedQuery, results);
        }

        return results;
    }

    /**
     * Search products with sorting applied.
     */
    public List<Product> searchProductsSorted(String query, SortCriteria sortCriteria) {
        List<Product> results = new ArrayList<>(searchProducts(query));
        ProductSorter.sort(results, sortCriteria);
        return results;
    }

    /**
     * Fast in-memory search using hash-based index (after initial load).
     * This is optimized for scenarios where data is already cached.
     */
    public List<Product> searchProductsInMemory(String query) {
        // Ensure index is built
        getAllProducts();
        return searcher.search(query);
    }

    /**
     * Create a new product with cache invalidation.
     */
    public void createProduct(Product product) {
        if (product.getPrice() != null && product.getPrice().doubleValue() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        productDAO.addProduct(product);

        // Invalidate cache since data changed
        if (cachingEnabled) {
            cache.invalidateAll();
            System.out.println("[Cache] Invalidated after product creation");
        }
    }

    /**
     * Update an existing product with cache invalidation.
     */
    public void updateProduct(Product product) {
        if (product.getPrice() != null && product.getPrice().doubleValue() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        productDAO.updateProduct(product);

        // Invalidate cache since data changed
        if (cachingEnabled) {
            cache.invalidateProduct(product.getProductId());
            System.out.println("[Cache] Invalidated product " + product.getProductId());
        }
    }

    /**
     * Delete a product with cache invalidation.
     */
    public void deleteProduct(int productId) {
        productDAO.deleteProduct(productId);

        // Invalidate cache since data changed
        if (cachingEnabled) {
            cache.invalidateProduct(productId);
            System.out.println("[Cache] Invalidated product " + productId);
        }
    }

    /**
     * Enable or disable caching (useful for performance comparison).
     */
    public void setCachingEnabled(boolean enabled) {
        this.cachingEnabled = enabled;
        if (!enabled) {
            cache.invalidateAll();
        }
    }

    /**
     * Check if caching is enabled.
     */
    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    /**
     * Get cache statistics for performance monitoring.
     */
    public ProductCache.CacheStats getCacheStats() {
        return cache.getStats();
    }

    /**
     * Get search index statistics.
     */
    public ProductSearcher.IndexStats getSearchIndexStats() {
        return searcher.getStats();
    }

    /**
     * Reset cache statistics.
     */
    public void resetCacheStats() {
        cache.resetStats();
    }
}
