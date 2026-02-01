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
    private boolean first;
    private boolean last;
    private boolean empty;
    private boolean hasNext;
    private boolean hasPrevious;
}
