package com.smecs.util;

import com.smecs.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for searching products using various algorithms.
 * Implements hashing-based search for optimized lookups.
 */
public class ProductSearcher {
    
    // Hash index for product name lookups (words -> products)
    private final Map<String, List<Product>> nameIndex;
    
    // Hash index for category lookups
    private final Map<String, List<Product>> categoryIndex;
    
    // Hash index for exact product name lookups
    private final Map<String, Product> exactNameIndex;
    
    public ProductSearcher() {
        this.nameIndex = new HashMap<>();
        this.categoryIndex = new HashMap<>();
        this.exactNameIndex = new HashMap<>();
    }
    
    /**
     * Build search indexes from a list of products.
     * This enables O(1) hash-based lookups for common searches.
     */
    public void buildIndex(List<Product> products) {
        nameIndex.clear();
        categoryIndex.clear();
        exactNameIndex.clear();
        
        for (Product product : products) {
            indexProduct(product);
        }
    }
    
    private void indexProduct(Product product) {
        // Index by exact product name (lowercase)
        if (product.getProductName() != null) {
            String lowerName = product.getProductName().toLowerCase();
            exactNameIndex.put(lowerName, product);
            
            // Index by individual words in product name
            String[] words = lowerName.split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    nameIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(product);
                }
            }
        }
        
        // Index by category name
        if (product.getCategoryName() != null) {
            String lowerCategory = product.getCategoryName().toLowerCase();
            categoryIndex.computeIfAbsent(lowerCategory, k -> new ArrayList<>()).add(product);
            
            // Also index by individual words in category
            String[] categoryWords = lowerCategory.split("\\s+");
            for (String word : categoryWords) {
                if (!word.isEmpty()) {
                    categoryIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(product);
                }
            }
        }
    }
    
    /**
     * Search products using the hash index.
     * Returns products matching the query in product name or category.
     */
    public List<Product> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerQuery = query.toLowerCase().trim();
        Map<Integer, Product> results = new HashMap<>();
        
        // First try exact name match
        Product exactMatch = exactNameIndex.get(lowerQuery);
        if (exactMatch != null) {
            results.put(exactMatch.getProductId(), exactMatch);
        }
        
        // Search by individual words
        String[] queryWords = lowerQuery.split("\\s+");
        for (String word : queryWords) {
            // Search in name index
            List<Product> nameMatches = nameIndex.get(word);
            if (nameMatches != null) {
                for (Product p : nameMatches) {
                    results.put(p.getProductId(), p);
                }
            }
            
            // Search in category index
            List<Product> categoryMatches = categoryIndex.get(word);
            if (categoryMatches != null) {
                for (Product p : categoryMatches) {
                    results.put(p.getProductId(), p);
                }
            }
        }
        
        // Also perform partial match for better results
        for (Map.Entry<String, List<Product>> entry : nameIndex.entrySet()) {
            if (entry.getKey().contains(lowerQuery) || lowerQuery.contains(entry.getKey())) {
                for (Product p : entry.getValue()) {
                    results.put(p.getProductId(), p);
                }
            }
        }
        
        for (Map.Entry<String, List<Product>> entry : categoryIndex.entrySet()) {
            if (entry.getKey().contains(lowerQuery) || lowerQuery.contains(entry.getKey())) {
                for (Product p : entry.getValue()) {
                    results.put(p.getProductId(), p);
                }
            }
        }
        
        return new ArrayList<>(results.values());
    }
    
    /**
     * Binary search for a product by name in a sorted list.
     * Assumes list is sorted by product name (case-insensitive).
     */
    public static int binarySearchByName(List<Product> sortedProducts, String targetName) {
        if (sortedProducts == null || sortedProducts.isEmpty() || targetName == null) {
            return -1;
        }
        
        String lowerTarget = targetName.toLowerCase();
        int left = 0;
        int right = sortedProducts.size() - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            String midName = sortedProducts.get(mid).getProductName();
            
            if (midName == null) {
                left = mid + 1;
                continue;
            }
            
            int comparison = midName.toLowerCase().compareTo(lowerTarget);
            
            if (comparison == 0) {
                return mid;
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return -1; // Not found
    }
    
    /**
     * Linear search with early termination (baseline for comparison).
     */
    public static Product linearSearch(List<Product> products, String targetName) {
        if (products == null || targetName == null) {
            return null;
        }
        
        String lowerTarget = targetName.toLowerCase();
        for (Product product : products) {
            if (product.getProductName() != null && 
                product.getProductName().toLowerCase().equals(lowerTarget)) {
                return product;
            }
        }
        
        return null;
    }
    
    /**
     * Get index statistics.
     */
    public IndexStats getStats() {
        return new IndexStats(
                nameIndex.size(),
                categoryIndex.size(),
                exactNameIndex.size()
        );
    }
    
    public static class IndexStats {
        public final int nameIndexSize;
        public final int categoryIndexSize;
        public final int exactNameIndexSize;
        
        public IndexStats(int nameIndexSize, int categoryIndexSize, int exactNameIndexSize) {
            this.nameIndexSize = nameIndexSize;
            this.categoryIndexSize = categoryIndexSize;
            this.exactNameIndexSize = exactNameIndexSize;
        }
        
        @Override
        public String toString() {
            return String.format("IndexStats{nameWords=%d, categories=%d, exactNames=%d}",
                    nameIndexSize, categoryIndexSize, exactNameIndexSize);
        }
    }
}

