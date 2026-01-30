//package com.smecs.cache;
//
//import com.smecs.model.Category;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * In-memory cache for categories with TTL-based invalidation and search-result caching.
// * Designed to mirror ProductCache behavior for consistency.
// */
//public class CategoryCache {
//    private static CategoryCache instance;
//
//    // Main category cache: categoryId -> Category
//    private final Map<Integer, Category> categoryById;
//
//    // Search result cache: searchQuery -> CachedSearchResult
//    private final Map<String, CachedSearchResult> searchCache;
//
//    // Indicates whether categoryById currently contains the complete category set
//    private volatile boolean allCategoriesLoaded = false;
//
//    // Statistics for performance monitoring
//    private long cacheHits = 0;
//    private long cacheMisses = 0;
//
//    private CategoryCache() {
//        this.categoryById = new ConcurrentHashMap<>();
//        this.searchCache = new ConcurrentHashMap<>();
//    }
//
//    public static synchronized CategoryCache getInstance() {
//        if (instance == null) {
//            instance = new CategoryCache();
//        }
//        return instance;
//    }
//
//    /**
//     * Get all cached categories (only if we know the cache contains the full set).
//     */
//    public Optional<List<Category>> getAllCategories() {
//        if (allCategoriesLoaded && !categoryById.isEmpty()) {
//            cacheHits++;
//            return Optional.of(new ArrayList<>(categoryById.values()));
//        }
//        cacheMisses++;
//        return Optional.empty();
//    }
//
//    /**
//     * Cache a single category.
//     */
//    public void putCategory(Category category) {
//        categoryById.put(category.getCategoryId(), category);
//    }
//
//    /**
//     * Cache all categories (populate categoryById from provided list).
//     */
//    public void putAllCategories(List<Category> categories) {
//        // Clear existing category storage (but keep search cache as-is)
//        categoryById.clear();
//
//        // Populate caches
//        categories.forEach(this::putCategory);
//
//        // Mark that categoryById now contains the full category set
//        allCategoriesLoaded = true;
//    }
//
//    /**
//     * Invalidate cache for a specific category.
//     */
//    public void invalidateCategory(int categoryId) {
//        categoryById.remove(categoryId);
//
//        // Any removal invalidates the "full set" flag
//        allCategoriesLoaded = false;
//    }
//
//    /**
//     * Invalidate entire cache.
//     */
//    public void invalidateAll() {
//        categoryById.clear();
//        searchCache.clear();
//        allCategoriesLoaded = false;
//        System.out.println("[Cache] All category cache invalidated");
//    }
//
//    /**
//     * Inner class for cached search results with timestamp.
//     */
//    private static class CachedSearchResult {
//        final List<Category> categories;
//        final long timestamp;
//
//        CachedSearchResult(List<Category> categories, long timestamp) {
//            this.categories = new ArrayList<>(categories);
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
//        public final int categoriesCached;
//        public final int searchQueriesCached;
//        public final boolean allCategoriesCached;
//
//        public CacheStats(long hits, long misses, int categoriesCached,
//                          int searchQueriesCached, boolean allCategoriesCached) {
//            this.hits = hits;
//            this.misses = misses;
//            this.categoriesCached = categoriesCached;
//            this.searchQueriesCached = searchQueriesCached;
//            this.allCategoriesCached = allCategoriesCached;
//        }
//
//        public double getHitRate() {
//            long total = hits + misses;
//            return total > 0 ? (double) hits / total * 100 : 0;
//        }
//
//        @Override
//        public String toString() {
//            return String.format("CacheStats{hits=%d, misses=%d, hitRate=%.2f%%, categories=%d, searches=%d}",
//                    hits, misses, getHitRate(), categoriesCached, searchQueriesCached);
//        }
//    }
//}
