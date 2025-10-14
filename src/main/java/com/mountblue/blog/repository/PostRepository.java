package com.mountblue.blog.repository;

import com.mountblue.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByAuthorNameContainingIgnoreCaseAndTagsContainingIgnoreCase(
            String authorName, String tag, Pageable pageable
    );

    Page<Post> findByAuthorNameContainingIgnoreCase(String author, Pageable pageable);

    Page<Post> findByTagsContaining(String tag, Pageable pageable);

    Page<Post> findByAuthorNameContainingIgnoreCaseAndTagsContaining(String author, String tag, Pageable pageable);

    Page<Post> findByAuthorNameInIgnoreCase(List<String> authorNames, Pageable pageable);

    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Post> findByAuthorNameIn(Set<String> authors, Pageable pageable);

    Page<Post> findByTagsIn(Set<String> tags, Pageable pageable);

    Page<Post> findByAuthorNameInAndTagsIn(Set<String> authors, Set<String> tags, Pageable pageable);

}

