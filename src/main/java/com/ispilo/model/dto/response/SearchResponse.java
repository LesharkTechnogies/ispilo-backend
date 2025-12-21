package com.ispilo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalResults;
    private int totalPages;

    public static <T> SearchResponse<T> of(Page<T> page) {
        return SearchResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalResults(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    public static <T> SearchResponse<T> empty() {
        return SearchResponse.<T>builder()
                .content(Collections.emptyList())
                .page(0)
                .size(0)
                .totalResults(0)
                .totalPages(0)
                .build();
    }
}
