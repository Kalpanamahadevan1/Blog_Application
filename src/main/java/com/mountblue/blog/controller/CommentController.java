package com.mountblue.blog.controller;

import com.mountblue.blog.entity.Comment;
import com.mountblue.blog.entity.Post;
import com.mountblue.blog.service.CommentService;
import com.mountblue.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    // Add Comment
    @PostMapping("/add/{postId}")
    public String addComment(@PathVariable Long postId,
                             @RequestParam String content,
                             @RequestParam String authorName) {

        Post post = postService.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setContent(content);
        comment.setAuthorName(authorName);
        comment.setCreatedAt(LocalDateTime.now());

        commentService.save(comment);
        return "redirect:/post/" + postId;
    }

    @GetMapping("/edit/{id}")
    public String editCommentForm(@PathVariable Long id, Model model) {
        Comment comment = commentService.findById(id);
        model.addAttribute("comment", comment);
        return "edit_comment"; // Thymeleaf template
    }

    @PostMapping("/edit/{id}")
    public String updateComment(@PathVariable Long id,
                                @RequestParam String content,
                                @RequestParam String authorName) {
        Comment comment = commentService.findById(id);
        if (comment != null) {
            comment.setContent(content);
            comment.setAuthorName(authorName);
            commentService.save(comment);  // Save updated comment
            return "redirect:/post/" + comment.getPost().getId();
        }
        throw new RuntimeException("Comment not found");
    }

    // Delete Comment
    @GetMapping("/delete/{id}")
    public String deleteComment(@PathVariable Long id) {
        Comment comment = commentService.findById(id);
        Long postId = comment.getPost().getId();
        commentService.deleteById(id);
        return "redirect:/post/" + postId;
    }
}
