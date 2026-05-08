package com.ispilo.service;

import com.ispilo.model.dto.response.GroupPostResponse;
import com.ispilo.model.entity.GroupPost;
import com.ispilo.repository.GroupPostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupPostMapper {

    private final GroupPostLikeRepository groupPostLikeRepository;

    public GroupPostResponse toDto(GroupPost p, boolean callerIsAdmin) {
        GroupPostResponse r = new GroupPostResponse();
        r.setId(p.getId());
        r.setText(p.getText());
        r.setMediaUrls(p.getMediaUrls());
        r.setAnonymous(p.isAnonymous());
        r.setCreatedAt(p.getCreatedAt());
        r.setLikeCount(groupPostLikeRepository.countByPost(p));

        if (callerIsAdmin) {
            r.setAuthorId(p.getAuthor() != null ? p.getAuthor().getId() : null);
            r.setAuthorName(p.getAuthor() != null ? p.getAuthor().getName() : null);
        } else if (p.isAnonymous()) {
            r.setAuthorId(null);
            r.setAuthorName("Anonymous");
        } else {
            r.setAuthorId(p.getAuthor() != null ? p.getAuthor().getId() : null);
            r.setAuthorName(p.getAuthor() != null ? p.getAuthor().getName() : null);
        }

        return r;
    }
}
