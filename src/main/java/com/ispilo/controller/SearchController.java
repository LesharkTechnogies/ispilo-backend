package com.ispilo.controller;

import com.ispilo.model.dto.response.PersonResponse;
import com.ispilo.model.dto.response.PostResponse;
import com.ispilo.model.dto.response.SearchResponse;
import com.ispilo.model.dto.response.TypeaheadResponse;
import com.ispilo.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/posts")
    public ResponseEntity<SearchResponse<PostResponse>> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(searchService.searchPosts(q, pageable));
    }

    @GetMapping("/people")
    public ResponseEntity<SearchResponse<PersonResponse>> searchPeople(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(searchService.searchPeople(q, pageable));
    }

    @GetMapping("/typeahead")
    public ResponseEntity<TypeaheadResponse> typeaheadSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchService.typeaheadSearch(q, limit));
    }
}
