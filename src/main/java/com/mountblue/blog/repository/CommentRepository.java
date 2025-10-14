package com.mountblue.blog.repository;

import com.mountblue.blog.entity.Comment;
import com.mountblue.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
}
