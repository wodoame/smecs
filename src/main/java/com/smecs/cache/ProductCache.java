//package com.smecs.cache;
//
//import com.smecs.model.Product;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * In-memory cache for products with TTL-based invalidation.
// * Implements caching layer using Maps for faster retrieval.
// */
//public class ProductCache {
//    private static ProductCache instance;
//
//    // Main product cache: productId -> Product
//    private final Map<Integer, Product> productById;
//
//    // Search result cache: searchQuery -> List<Product>
//    private final Map<String, CachedSearchResult> searchCache;
//
//    // Cache configuration
//    private static final long SEARCH_CACHE_TTL_MS = 2 * 60 * 1000; // 2 minutes
//    private static final int MAX_SEARCH_CACHE_SIZE = 100;
//
//    // Indicates whether productById currently contains the complete product set
//    private volatile boolean allProductsLoaded = false;
//
//    // Statistics for performance monitoring
//    private long cacheHits = 0;
//    private long cacheMisses = 0;
//
//    private ProductCache() {
//        this.productById = new ConcurrentHashMap<>();
//        this.searchCache = new ConcurrentHashMap<>();
//    }
//
//    public static synchronized ProductCache getInstance() {
//        if (instance == null) {
//            instance = new ProductCache();
//        }
//        return instance;
//    }
//
//    /**
//     * Get all cached products (built from productById) only if we know the map contains the full set.
//     */
//    public Optional<List<Product>> getAllProducts() {
//        if (allProductsLoaded && !productById.isEmpty()) {
//            cacheHits++;
//            // Return a defensive copy so callers can't mutate internal map values
//            return Optional.of(new ArrayList<>(productById.values()));
//        }
//        cacheMisses++;
//        return Optional.empty();
//    }
//
//    /**
//     * Get cached search results if available and valid.
//     */
//    public Optional<List<Product>> getSearchResults(String query) {
//        String normalizedQuery = normalizeQuery(query);
//        CachedSearchResult cached = searchCache.get(normalizedQuery);
//
//        if (cached != null && !isCacheExpired(cached.timestamp, SEARCH_CACHE_TTL_MS)) {
//            cacheHits++;
//            return Optional.of(new ArrayList<>(cached.products));
//        }
//        cacheMisses++;
//        return Optional.empty();
//    }
//
//    /**
//     * Cache a single product.
//     */
//    public void putProduct(Product product) {
//        productById.put(product.getProductId(), product);
//    }
//
//    /**
//     * Cache all products (populate productById from provided list).
//     */
//    public void putAllProducts(List<Product> products) {
//        // Clear existing product storage (but keep search cache as-is)
//        productById.clear();
//
//        // Populate caches
//        products.forEach(this::putProduct);
//
//        // Mark that productById now contains the full product set
//        allProductsLoaded = true;
//    }
//
//    /**
//     * Cache search results.
//     */
//    public void putSearchResults(String query, List<Product> results) {
//        // Limit search cache size
//        if (searchCache.size() >= MAX_SEARCH_CACHE_SIZE) {
//            evictOldestSearchResults();
//        }
//
//        String normalizedQuery = normalizeQuery(query);
//        searchCache.put(normalizedQuery, new CachedSearchResult(results, System.currentTimeMillis()));
//
//        // Also cache individual products from search results
//        results.forEach(this::putProduct);
//    }
//
//    /**
//     * Invalidate cache for a specific product.
//     */
//    public void invalidateProduct(int productId) {
//        productById.remove(productId);
//
//        // Any removal invalidates the "full set" flag
//        allProductsLoaded = false;
//
//        // Previously we cleared an all-products snapshot and search cache; keep search cache as-is
//        // (search cache may still reference stale results, handle separately if needed)
//    }
//
//    /**
//     * Invalidate entire cache.
//     */
//    public void invalidateAll() {
//        productById.clear();
//        searchCache.clear();
//        allProductsLoaded = false;
//        System.out.println("[Cache] All product cache invalidated");
//    }
//
//    /**
//     * Get cache statistics.
//     */
//    public CacheStats getStats() {
//        return new CacheStats(
//                cacheHits,
//                cacheMisses,
//                productById.size(),
//                searchCache.size(),
//                allProductsLoaded
//        );
//    }
//
//    /**
//     * Reset cache statistics.
//     */
//    public void resetStats() {
//        cacheHits = 0;
//        cacheMisses = 0;
//    }
//
//    private boolean isCacheExpired(long timestamp, long ttl) {
//        return System.currentTimeMillis() - timestamp > ttl;
//    }
//
//    private String normalizeQuery(String query) {
//        return query == null ? "" : query.toLowerCase().trim();
//    }
//
//    private void evictOldestSearchResults() {
//        // Remove oldest 25% of search cache entries
//        int toRemove = MAX_SEARCH_CACHE_SIZE / 4;
//        searchCache.entrySet().stream()
//                .sorted(Comparator.comparingLong(e -> e.getValue().timestamp))
//                .limit(toRemove)
//                .map(Map.Entry::getKey)
//                .forEach(searchCache::remove);
//    }
//
//    /**
//     * Inner class for cached search results with timestamp.
//     */
//    private static class CachedSearchResult {
//        final List<Product> products;
//        final long timestamp;
//
//        CachedSearchResult(List<Product> products, long timestamp) {
//            this.products = new ArrayList<>(products);
//            this.timestamp = timestamp;
//        }
//    }
//
//    /**
//     * Cache statistics record.
//     */
//    public static class CacheStats {
//        public final long hits;
//        public final long misses;
//        public final int productsCached;
//        public final int searchQueriesCached;
//        public final boolean allProductsCached;
//
//        public CacheStats(long hits, long misses, int productsCached,
//                         int searchQueriesCached, boolean allProductsCached) {
//            this.hits = hits;
//            this.misses = misses;
//            this.productsCached = productsCached;
//            this.searchQueriesCached = searchQueriesCached;
//            this.allProductsCached = allProductsCached;
//        }
//
//        public double getHitRate() {
//            long total = hits + misses;
//            return total > 0 ? (double) hits / total * 100 : 0;
//        }
//
//        @Override
//        public String toString() {
//            return String.format("CacheStats{hits=%d, misses=%d, hitRate=%.2f%%, products=%d, searches=%d}",
//                    hits, misses, getHitRate(), productsCached, searchQueriesCached);
//        }
//    }
//}
