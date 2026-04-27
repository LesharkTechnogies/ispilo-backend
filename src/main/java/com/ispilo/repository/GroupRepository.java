package com.ispilo.repository;

import com.ispilo.model.entity.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<GroupEntity, String> {

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT g FROM GroupEntity g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(g.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<GroupEntity> searchGroups(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT name FROM groups WHERE LOWER(name) LIKE LOWER(CONCAT('%', :query, '%')) LIMIT :limit", nativeQuery = true)
    List<String> findTypeaheadSuggestions(@Param("query") String query, @Param("limit") int limit);
}
