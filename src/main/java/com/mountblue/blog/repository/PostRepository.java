package com.mountblue.blog.repository;

import com.mountblue.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(p.authorName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(t) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<Post> fullTextSearch(@Param("searchText") String searchText, Pageable pageable);

    @Query(value = "SELECT DISTINCT p.* FROM posts p " +
            "LEFT JOIN tags t ON p.id = t.post_id " +
            "WHERE (:#{#authors.size()} = 0 OR p.author_name IN (:authors)) " +
            "AND (:#{#tags.size()} = 0 OR t.name IN (:tags)) " +
            "AND (:startDate IS NULL OR p.published_at >= CAST(:startDate AS timestamp)) " +
            "AND (:endDate IS NULL OR p.published_at <= CAST(:endDate AS timestamp)) " +
            "ORDER BY p.published_at DESC",
            countQuery = "SELECT COUNT(DISTINCT p.id) FROM posts p " +
                    "LEFT JOIN tags t ON p.id = t.post_id " +
                    "WHERE (:#{#authors.size()} = 0 OR p.author_name IN (:authors)) " +
                    "AND (:#{#tags.size()} = 0 OR t.name IN (:tags)) " +
                    "AND (:startDate IS NULL OR p.published_at >= CAST(:startDate AS timestamp)) " +
                    "AND (:endDate IS NULL OR p.published_at <= CAST(:endDate AS timestamp))",
            nativeQuery = true)
    Page<Post> findWithFilters(
            @Param("authors") Set<String> authors,
            @Param("tags") Set<String> tags,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            Pageable pageable
    );
}
