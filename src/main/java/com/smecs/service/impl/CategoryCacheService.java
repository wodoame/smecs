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
    public Optional<PagedResponseDTO<CategoryDTO>> getSearchResults(String query, int page, int size, String sort) {
        return getSearchResults(query, page, size, sort, false);
    }

    public Optional<PagedResponseDTO<CategoryDTO>> getSearchResults(String query, int page, int size, String sort,
            boolean relatedImages) {
        String key = createSearchKey(query, page, size, sort, relatedImages);
        CacheEntry<PagedResponseDTO<CategoryDTO>> entry = searchCache.get(key);
        if (entry == null || entry.isExpired()) {
            searchCache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void putSearchResults(String query, int page, int size, String sort, PagedResponseDTO<CategoryDTO> result) {
        putSearchResults(query, page, size, sort, false, result);
    }

    public void putSearchResults(String query, int page, int size, String sort, boolean relatedImages,
            PagedResponseDTO<CategoryDTO> result) {
        if (result != null && query != null) {
            String key = createSearchKey(query, page, size, sort, relatedImages);
            searchCache.put(key, new CacheEntry<>(result, DEFAULT_TTL_MS));
        }
    }

    private String createSearchKey(String query, int page, int size, String sort) {
        return createSearchKey(query, page, size, sort, false);
    }

    private String createSearchKey(String query, int page, int size, String sort, boolean relatedImages) {
        return String.format("%s|%d|%d|%s|%b", query, page, size, sort, relatedImages);
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
}
