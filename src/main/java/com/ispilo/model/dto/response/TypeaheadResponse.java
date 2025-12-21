package com.ispilo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeaheadResponse {
    private String query;
    private List<TypeaheadItem> suggestions;

    public static TypeaheadResponse empty() {
        return TypeaheadResponse.builder()
                .suggestions(Collections.emptyList())
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TypeaheadItem {
        private String type; // "post", "user", "seller", "hashtag"
        private String text;
    }
}
