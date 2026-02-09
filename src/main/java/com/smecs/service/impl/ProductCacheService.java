package com.smecs.service.impl;

import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.service.CacheService;
import com.smecs.service.SearchCacheService;
import com.smecs.util.CacheEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for product requests with a simple TTL policy.
 * Only individual product lookups and search result pages are cached.
 */
@Service
public class ProductCacheService implements CacheService<ProductDTO, Long>, SearchCacheService<ProductDTO> {
    private static final long DEFAULT_TTL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<Long, CacheEntry<ProductDTO>> productById = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<PagedResponseDTO<ProductDTO>>> searchCache = new ConcurrentHashMap<>();

    @Override
    public Optional<ProductDTO> getById(Long id) {
        CacheEntry<ProductDTO> entry = productById.get(id);
        if (entry == null || entry.isExpired()) {
            productById.remove(id);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void put(ProductDTO product) {
        if (product == null || product.getId() == null) {
            return;
        }
        productById.put(product.getId(), new CacheEntry<>(product, DEFAULT_TTL_MS));
    }

    @Override
    public Optional<PagedResponseDTO<ProductDTO>> getSearchResults(String query, int page, int size, String sort) {
        String key = createSearchKey(query, page, size, sort);
        CacheEntry<PagedResponseDTO<ProductDTO>> entry = searchCache.get(key);
        if (entry == null || entry.isExpired()) {
            searchCache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void putSearchResults(String query, int page, int size, String sort, PagedResponseDTO<ProductDTO> result) {
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
            productById.remove(id);
            searchCache.clear();
        }
    }

    @Override
    public void invalidateAllList() {
        searchCache.clear();
    }
}
