package com.smecs.service;

import com.smecs.dto.PagedResponseDTO;

import java.util.Optional;

public interface SearchCacheService<T> {
    Optional<PagedResponseDTO<T>> getSearchResults(String query, int page, int size, String sort);

    void putSearchResults(String query, int page, int size, String sort, PagedResponseDTO<T> result);
}
