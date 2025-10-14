package com.mountblue.blog.service;

import com.mountblue.blog.entity.Comment;
import com.mountblue.blog.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    public Comment updateComment(Long id, String content, String authorName) {
        Comment comment = findById(id);
        comment.setContent(content);
        comment.setAuthorName(authorName);
        return save(comment);
    }
}
