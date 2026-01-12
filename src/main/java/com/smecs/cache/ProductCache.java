package com.smecs.cache;

import com.smecs.model.Product;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory cache for products with TTL-based invalidation.
 * Implements caching layer using Maps for faster retrieval.
 */
public class ProductCache {
    private static ProductCache instance;
    
    // Main product cache: productId -> Product
    private final Map<Integer, Product> productById;
    
    // Index for category-based lookups: categoryId -> List<productId>
    private final Map<Integer, Set<Integer>> productsByCategory;
    
    // Search result cache: searchQuery -> List<Product>
    private final Map<String, CachedSearchResult> searchCache;
    
    // Cache for all products
    private List<Product> allProductsCache;
    private long allProductsCacheTimestamp;
    
    // Cache configuration
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private static final long SEARCH_CACHE_TTL_MS = 2 * 60 * 1000; // 2 minutes
    private static final int MAX_SEARCH_CACHE_SIZE = 100;
    
    // Statistics for performance monitoring
    private long cacheHits = 0;
    private long cacheMisses = 0;
    
    private ProductCache() {
        this.productById = new ConcurrentHashMap<>();
        this.productsByCategory = new ConcurrentHashMap<>();
        this.searchCache = new ConcurrentHashMap<>();
        this.allProductsCache = null;
        this.allProductsCacheTimestamp = 0;
    }
    
    public static synchronized ProductCache getInstance() {
        if (instance == null) {
            instance = new ProductCache();
        }
        return instance;
    }
    
    /**
     * Get a product by ID from cache.
     */
    public Optional<Product> getProduct(int productId) {
        Product product = productById.get(productId);
        if (product != null) {
            cacheHits++;
            return Optional.of(product);
        }
        cacheMisses++;
        return Optional.empty();
    }
    
    /**
     * Get all cached products if cache is valid.
     */
    public Optional<List<Product>> getAllProducts() {
        if (allProductsCache != null && !isCacheExpired(allProductsCacheTimestamp, CACHE_TTL_MS)) {
            cacheHits++;
            return Optional.of(new ArrayList<>(allProductsCache));
        }
        cacheMisses++;
        return Optional.empty();
    }
    
    /**
     * Get cached search results if available and valid.
     */
    public Optional<List<Product>> getSearchResults(String query) {
        String normalizedQuery = normalizeQuery(query);
        CachedSearchResult cached = searchCache.get(normalizedQuery);
        
        if (cached != null && !isCacheExpired(cached.timestamp, SEARCH_CACHE_TTL_MS)) {
            cacheHits++;
            return Optional.of(new ArrayList<>(cached.products));
        }
        cacheMisses++;
        return Optional.empty();
    }
    
    /**
     * Get products by category from cache.
     */
    public Optional<List<Product>> getProductsByCategory(int categoryId) {
        Set<Integer> productIds = productsByCategory.get(categoryId);
        if (productIds != null && !productIds.isEmpty()) {
            List<Product> products = productIds.stream()
                    .map(productById::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (!products.isEmpty()) {
                cacheHits++;
                return Optional.of(products);
            }
        }
        cacheMisses++;
        return Optional.empty();
    }
    
    /**
     * Cache a single product.
     */
    public void putProduct(Product product) {
        productById.put(product.getProductId(), product);
        
        // Update category index
        productsByCategory
                .computeIfAbsent(product.getCategoryId(), k -> ConcurrentHashMap.newKeySet())
                .add(product.getProductId());
    }
    
    /**
     * Cache all products.
     */
    public void putAllProducts(List<Product> products) {
        // Clear existing cache
        productById.clear();
        productsByCategory.clear();
        
        // Populate caches
        for (Product product : products) {
            putProduct(product);
        }
        
        allProductsCache = new ArrayList<>(products);
        allProductsCacheTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Cache search results.
     */
    public void putSearchResults(String query, List<Product> results) {
        // Limit search cache size
        if (searchCache.size() >= MAX_SEARCH_CACHE_SIZE) {
            evictOldestSearchResults();
        }
        
        String normalizedQuery = normalizeQuery(query);
        searchCache.put(normalizedQuery, new CachedSearchResult(results, System.currentTimeMillis()));
        
        // Also cache individual products from search results
        for (Product product : results) {
            putProduct(product);
        }
    }
    
    /**
     * Invalidate cache for a specific product.
     */
    public void invalidateProduct(int productId) {
        Product removed = productById.remove(productId);
        if (removed != null) {
            Set<Integer> categoryProducts = productsByCategory.get(removed.getCategoryId());
            if (categoryProducts != null) {
                categoryProducts.remove(productId);
            }
        }
        
        // Invalidate all products cache and search cache since data changed
        allProductsCache = null;
        allProductsCacheTimestamp = 0;
        searchCache.clear();
    }
    
    /**
     * Invalidate entire cache.
     */
    public void invalidateAll() {
        productById.clear();
        productsByCategory.clear();
        searchCache.clear();
        allProductsCache = null;
        allProductsCacheTimestamp = 0;
        System.out.println("[Cache] All product cache invalidated");
    }
    
    /**
     * Get cache statistics.
     */
    public CacheStats getStats() {
        return new CacheStats(
                cacheHits,
                cacheMisses,
                productById.size(),
                searchCache.size(),
                allProductsCache != null
        );
    }
    
    /**
     * Reset cache statistics.
     */
    public void resetStats() {
        long t = 1;
        cacheHits = 0;
        cacheMisses = 0;
    }

    private boolean isCacheExpired(long timestamp, long ttl) {
        return System.currentTimeMillis() - timestamp > ttl;
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.toLowerCase().trim();
    }
    
    private void evictOldestSearchResults() {
        // Remove oldest 25% of search cache entries
        int toRemove = MAX_SEARCH_CACHE_SIZE / 4;
        searchCache.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getValue().timestamp))
                .limit(toRemove)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .forEach(searchCache::remove);
    }
    
    /**
     * Inner class for cached search results with timestamp.
     */
    private static class CachedSearchResult {
        final List<Product> products;
        final long timestamp;
        
        CachedSearchResult(List<Product> products, long timestamp) {
            this.products = new ArrayList<>(products);
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Cache statistics record.
     */
    public static class CacheStats {
        public final long hits;
        public final long misses;
        public final int productsCached;
        public final int searchQueriesCached;
        public final boolean allProductsCached;
        
        public CacheStats(long hits, long misses, int productsCached, 
                         int searchQueriesCached, boolean allProductsCached) {
            this.hits = hits;
            this.misses = misses;
            this.productsCached = productsCached;
            this.searchQueriesCached = searchQueriesCached;
            this.allProductsCached = allProductsCached;
        }
        
        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{hits=%d, misses=%d, hitRate=%.2f%%, products=%d, searches=%d}",
                    hits, misses, getHitRate(), productsCached, searchQueriesCached);
        }
    }
}

