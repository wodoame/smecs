package com.smecs.service.impl;

import com.smecs.dto.CategoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.service.CacheService;
import com.smecs.service.SearchCacheService;
import com.smecs.util.CacheEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for category requests with a simple TTL policy.
 * Only individual category lookups and search result pages are cached.
 */
@Service
public class CategoryCacheService implements CacheService<CategoryDTO, Long>, SearchCacheService<CategoryDTO> {
    private static final long DEFAULT_TTL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<Long, CacheEntry<CategoryDTO>> categoryById = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<PagedResponseDTO<CategoryDTO>>> searchCache = new ConcurrentHashMap<>();

    @Override
    public Optional<CategoryDTO> getById(Long id) {
        CacheEntry<CategoryDTO> entry = categoryById.get(id);
        if (entry == null || entry.isExpired()) {
            categoryById.remove(id);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void put(CategoryDTO category) {
        if (category == null || category.getCategoryId() == null) {
            return;
        }
        categoryById.put(Long.valueOf(category.getCategoryId()), new CacheEntry<>(category, DEFAULT_TTL_MS));
    }

    @Override
    public void putAll(List<CategoryDTO> categories) {
        if (categories == null) {
            return;
        }
        categories.forEach(this::put);
    }

    @Override
    public Optional<PagedResponseDTO<CategoryDTO>> getSearchResults(String query, int page, int size, String sort) {
        String key = createSearchKey(query, page, size, sort);
        CacheEntry<PagedResponseDTO<CategoryDTO>> entry = searchCache.get(key);
        if (entry == null || entry.isExpired()) {
            searchCache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void putSearchResults(String query, int page, int size, String sort, PagedResponseDTO<CategoryDTO> result) {
        if (result != null && query != null) {
            String key = createSearchKey(query, page, size, sort);
            searchCache.put(key, new CacheEntry<>(result, DEFAULT_TTL_MS));
        }
    }

    private String createSearchKey(String query, int page, int size, String sort) {
        return String.format("%s|%d|%d|%s", query, page, size, sort);
    }

    @Override
    public void invalidateById(Long id) {
        if (id != null) {
            categoryById.remove(id);
            searchCache.clear();
        }
    }

    @Override
    public void invalidateAllList() {
        searchCache.clear();
    }

    @Override
    public void invalidateAll() {
        categoryById.clear();
        searchCache.clear();
    }
}
