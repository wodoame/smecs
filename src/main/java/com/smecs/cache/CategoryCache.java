package com.smecs.cache;

import com.smecs.model.Category;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for categories with TTL-based invalidation.
 */
public class CategoryCache {
    private static CategoryCache instance;
    
    // Category cache: categoryId -> Category
    private final Map<Integer, Category> categoryById;
    
    // Name lookup index: categoryName (lowercase) -> Category
    private final Map<String, Category> categoryByName;
    
    // All categories cache
    private List<Category> allCategoriesCache;
    private long allCategoriesCacheTimestamp;
    
    // Cache configuration
    private static final long CACHE_TTL_MS = 10 * 60 * 1000; // 10 minutes (categories change less frequently)
    
    // Statistics
    private long cacheHits = 0;
    private long cacheMisses = 0;
    
    private CategoryCache() {
        this.categoryById = new ConcurrentHashMap<>();
        this.categoryByName = new ConcurrentHashMap<>();
        this.allCategoriesCache = null;
        this.allCategoriesCacheTimestamp = 0;
    }
    
    public static synchronized CategoryCache getInstance() {
        if (instance == null) {
            instance = new CategoryCache();
        }
        return instance;
    }
    
    /**
     * Get a category by ID.
     */
    public Optional<Category> getCategory(int categoryId) {
        Category category = categoryById.get(categoryId);
        if (category != null) {
            cacheHits++;
            return Optional.of(category);
        }
        cacheMisses++;
        return Optional.empty();
    }
    
    /**
     * Get a category by name.
     */
    public Optional<Category> getCategoryByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        
        Category category = categoryByName.get(name.toLowerCase());
        if (category != null) {
            cacheHits++;
            return Optional.of(category);
        }
        cacheMisses++;
        return Optional.empty();
    }
    
    /**
     * Get all cached categories if cache is valid.
     */
    public Optional<List<Category>> getAllCategories() {
        if (allCategoriesCache != null && !isCacheExpired()) {
            cacheHits++;
            return Optional.of(new ArrayList<>(allCategoriesCache));
        }
        cacheMisses++;
        return Optional.empty();
    }
    
    /**
     * Cache a single category.
     */
    public void putCategory(Category category) {
        categoryById.put(category.getCategoryId(), category);
        if (category.getCategoryName() != null) {
            categoryByName.put(category.getCategoryName().toLowerCase(), category);
        }
    }
    
    /**
     * Cache all categories.
     */
    public void putAllCategories(List<Category> categories) {
        categoryById.clear();
        categoryByName.clear();
        
        for (Category category : categories) {
            putCategory(category);
        }
        
        allCategoriesCache = new ArrayList<>(categories);
        allCategoriesCacheTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Invalidate cache for a specific category.
     */
    public void invalidateCategory(int categoryId) {
        Category removed = categoryById.remove(categoryId);
        if (removed != null && removed.getCategoryName() != null) {
            categoryByName.remove(removed.getCategoryName().toLowerCase());
        }
        
        // Invalidate all categories cache
        allCategoriesCache = null;
        allCategoriesCacheTimestamp = 0;
    }
    
    /**
     * Invalidate entire cache.
     */
    public void invalidateAll() {
        categoryById.clear();
        categoryByName.clear();
        allCategoriesCache = null;
        allCategoriesCacheTimestamp = 0;
        System.out.println("[Cache] All category cache invalidated");
    }
    
    /**
     * Get cache statistics.
     */
    public CacheStats getStats() {
        return new CacheStats(
                cacheHits,
                cacheMisses,
                categoryById.size(),
                allCategoriesCache != null
        );
    }
    
    /**
     * Reset statistics.
     */
    public void resetStats() {
        cacheHits = 0;
        cacheMisses = 0;
    }
    
    private boolean isCacheExpired() {
        return System.currentTimeMillis() - allCategoriesCacheTimestamp > CACHE_TTL_MS;
    }
    
    public static class CacheStats {
        public final long hits;
        public final long misses;
        public final int categoriesCached;
        public final boolean allCategoriesCached;
        
        public CacheStats(long hits, long misses, int categoriesCached, boolean allCategoriesCached) {
            this.hits = hits;
            this.misses = misses;
            this.categoriesCached = categoriesCached;
            this.allCategoriesCached = allCategoriesCached;
        }
        
        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("CategoryCacheStats{hits=%d, misses=%d, hitRate=%.2f%%, categories=%d}",
                    hits, misses, getHitRate(), categoriesCached);
        }
    }
}

