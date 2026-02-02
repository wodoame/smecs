package com.smecs.service;

import java.util.List;
import java.util.Optional;

public interface CacheService<V, K> {
    Optional<V> getById(K id);

    void put(V value);

    void putAll(List<V> values);

    void invalidateById(K id);

    void invalidateAllList();

    void invalidateAll();
}
