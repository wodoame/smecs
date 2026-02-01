package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PageMetadataDTO {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;
    private boolean hasNext;
    private boolean hasPrevious;
}
