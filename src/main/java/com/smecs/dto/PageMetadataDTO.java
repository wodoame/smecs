package com.smecs.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

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

    public static PageMetadataDTO from(Page<?> page) {
        PageMetadataDTO metadata = new PageMetadataDTO();
        metadata.setPage(page.getNumber());
        metadata.setSize(page.getSize());
        metadata.setTotalElements(page.getTotalElements());
        metadata.setTotalPages(page.getTotalPages());
        metadata.setFirst(page.isFirst());
        metadata.setLast(page.isLast());
        metadata.setEmpty(page.isEmpty());
        metadata.setHasNext(page.hasNext());
        metadata.setHasPrevious(page.hasPrevious());
        return metadata;
    }
}
