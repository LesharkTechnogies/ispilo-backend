package com.ispilo.repository;

import com.ispilo.model.entity.Hashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, String> {
    Optional<Hashtag> findByNameIgnoreCase(String name);
    
    @Query("SELECT h FROM Hashtag h ORDER BY h.usageCount DESC")
    Page<Hashtag> findTrendingHashtags(Pageable pageable);
}
