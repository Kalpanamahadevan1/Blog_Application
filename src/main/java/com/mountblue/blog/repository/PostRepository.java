package com.mountblue.blog.repository;

import com.mountblue.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByAuthorNameAndTags(
            String authorName, String tag, Pageable pageable
    );
    Page<Post> findByAuthorName(String author, Pageable pageable);
    Page<Post> findByTags(String tag, Pageable pageable);
    Page<Post> findByAuthorNameContainingIgnoreCaseAndTagsContaining(String author, String tag, Pageable pageable);
    Page<Post> findByAuthorNameInAndTagsInAndTitleContainingIgnoreCase(
            List<String> authors, List<String> tags, String title, Pageable pageable);

    @Query("SELECT DISTINCT p.authorName FROM Post p ORDER BY p.authorName ASC")
    List<String> findAllAuthors();

    // Get all distinct tags
    @Query("SELECT DISTINCT t FROM Post p JOIN p.tags t ORDER BY t ASC")
    List<String> findAllTags();


}

