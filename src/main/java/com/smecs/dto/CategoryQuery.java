package com.smecs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryQuery {
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 10;
    public static final String DEFAULT_SORT = "name,asc";

    private String name;
    private String description;
    private Integer page;
    private Integer size;
    private String sort;
    @Builder.Default
    private boolean includeRelatedImages = false;

    public int currentPage() {
        return (page != null && page > 0) ? page : DEFAULT_PAGE;
    }

    public int currentSize() {
        return (size != null && size > 0) ? size : DEFAULT_SIZE;
    }

    public String sortOrDefault() {
        return (sort != null && !sort.isBlank()) ? sort : DEFAULT_SORT;
    }

    public String searchTerm() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (description != null && !description.isBlank()) {
            return description;
        }
        return "";
    }
}

