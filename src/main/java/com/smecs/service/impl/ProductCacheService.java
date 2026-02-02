package com.smecs.service.impl;

import com.smecs.dto.ProductDTO;
import com.smecs.service.CacheService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for product requests with a simple TTL policy.
 * Note: Paginated/filtered queries are not cached due to high variability.
 * Only individual product lookups and full list (getAllProducts) are cached.
 */
@Service
public class ProductCacheService implements CacheService<ProductDTO, Long> {
    private static final long DEFAULT_TTL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<Long, CacheEntry<ProductDTO>> productById = new ConcurrentHashMap<>();
    private volatile CacheEntry<List<ProductDTO>> allProductsEntry;

    @Override
    public Optional<ProductDTO> getById(Long id) {
        CacheEntry<ProductDTO> entry = productById.get(id);
        if (entry == null || entry.isExpired()) {
            productById.remove(id);
            return Optional.empty();
        }
        return Optional.of(entry.value);
    }

    @Override
    public void put(ProductDTO product) {
        if (product == null || product.getId() == null) {
            return;
        }
        productById.put(product.getId(), new CacheEntry<>(product, DEFAULT_TTL_MS));
    }

    @Override
    public Optional<List<ProductDTO>> getAll() {
        CacheEntry<List<ProductDTO>> entry = allProductsEntry;
        if (entry == null || entry.isExpired()) {
            allProductsEntry = null;
            return Optional.empty();
        }
        return Optional.of(new ArrayList<>(entry.value));
    }

    @Override
    public void putAll(List<ProductDTO> products) {
        if (products == null) {
            return;
        }
        allProductsEntry = new CacheEntry<>(new ArrayList<>(products), DEFAULT_TTL_MS);
        products.forEach(this::put);
    }

    @Override
    public void invalidateById(Long id) {
        if (id != null) {
            productById.remove(id);
        }
    }

    @Override
    public void invalidateAllList() {
        allProductsEntry = null;
    }

    @Override
    public void invalidateAll() {
        productById.clear();
        allProductsEntry = null;
    }

    private record CacheEntry<T>(T value, long expiresAtMs) {
            private CacheEntry(T value, long expiresAtMs) {
                this.value = value;
                this.expiresAtMs = System.currentTimeMillis() + expiresAtMs;
            }

            private boolean isExpired() {
                return System.currentTimeMillis() > expiresAtMs;
            }
        }
}
