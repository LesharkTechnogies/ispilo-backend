package com.ispilo.service;

import com.ispilo.model.dto.response.PersonResponse;
import com.ispilo.model.dto.response.PostResponse;
import com.ispilo.model.dto.response.SearchResponse;
import com.ispilo.model.dto.response.TypeaheadResponse;
import com.ispilo.model.entity.Post;
import com.ispilo.model.entity.User;
import com.ispilo.repository.PostRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public SearchResponse<PostResponse> searchPosts(String query, Pageable pageable) {
        Page<Post> posts = postRepository.searchPosts(query, pageable);
        return SearchResponse.of(posts.map(PostResponse::fromEntity));
    }

    public SearchResponse<PersonResponse> searchPeople(String query, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(query, pageable);
        return SearchResponse.of(users.map(this::convertToPersonResponse));
    }

    public TypeaheadResponse typeaheadSearch(String query, int limit) {
        String likeQuery = "%" + query + "%";
        List<String> postSuggestions = postRepository.findTypeaheadSuggestions(likeQuery, limit);
        List<String> userSuggestions = userRepository.findTypeaheadSuggestions(likeQuery, limit);

        List<TypeaheadResponse.TypeaheadItem> items = new ArrayList<>();
        postSuggestions.forEach(s -> items.add(new TypeaheadResponse.TypeaheadItem("post", s)));
        userSuggestions.forEach(s -> items.add(new TypeaheadResponse.TypeaheadItem("user", s)));

        return TypeaheadResponse.builder()
                .query(query)
                .suggestions(items.stream().limit(limit).collect(Collectors.toList()))
                .build();
    }

    private PersonResponse convertToPersonResponse(User user) {
        return PersonResponse.builder()
                .id(user.getId())
                .type("user")
                .name(user.getName())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .isVerified(user.getIsVerified())
                .location(user.getLocation())
                .build();
    }
}
