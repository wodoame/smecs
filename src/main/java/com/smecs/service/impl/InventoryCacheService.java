package com.smecs.service.impl;

import com.smecs.dto.InventoryDTO;
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
 * In-memory cache for inventory requests with a simple TTL policy.
 * Only individual inventory lookups and search result pages are cached.
 */
@Service
public class InventoryCacheService implements CacheService<InventoryDTO, Long>, SearchCacheService<InventoryDTO> {
    private static final long DEFAULT_TTL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<Long, CacheEntry<InventoryDTO>> inventoryById = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<PagedResponseDTO<InventoryDTO>>> searchCache = new ConcurrentHashMap<>();

    @Override
    public Optional<InventoryDTO> getById(Long id) {
        CacheEntry<InventoryDTO> entry = inventoryById.get(id);
        if (entry == null || entry.isExpired()) {
            inventoryById.remove(id);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void put(InventoryDTO inventory) {
        if (inventory == null || inventory.getId() == null) {
            return;
        }
        inventoryById.put(inventory.getId(), new CacheEntry<>(inventory, DEFAULT_TTL_MS));
    }

    @Override
    public Optional<PagedResponseDTO<InventoryDTO>> getSearchResults(String query, int page, int size, String sort) {
        String key = createSearchKey(query, page, size, sort);
        CacheEntry<PagedResponseDTO<InventoryDTO>> entry = searchCache.get(key);
        if (entry == null || entry.isExpired()) {
            searchCache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void putSearchResults(String query, int page, int size, String sort, PagedResponseDTO<InventoryDTO> result) {
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
            inventoryById.remove(id);
            searchCache.clear();
        }
    }

    @Override
    public void invalidateAllList() {
        searchCache.clear();
    }
}
