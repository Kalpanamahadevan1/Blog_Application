package com.mountblue.blog.controller;

import com.mountblue.blog.entity.Comment;
import com.mountblue.blog.entity.Post;
import com.mountblue.blog.service.CommentService;
import com.mountblue.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @PostMapping("/comment/add/{postId}")
    public String addComment(@PathVariable Long postId,
                             @RequestParam("authorName") String authorName,
                             @RequestParam("email") String email,
                             @RequestParam("content") String content) {
        Post post = postService.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setAuthorName(authorName);
        comment.setEmail(email);
        comment.setContent(content);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());

        commentService.save(comment);
        return "redirect:/post/" + postId;
    }

    @GetMapping("/comment/edit/{id}")
    public String editCommentForm(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");

        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (isAuthenticated) {
            String userEmail = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            boolean isPostAuthor = comment.getPost().getAuthorEmail().equals(userEmail);

            if (!isAdmin && !isPostAuthor) {
                throw new AccessDeniedException("You don't have permission to edit this comment");
            }
        }

        model.addAttribute("comment", comment);
        return "edit_comment";
    }

    @PostMapping("/comment/edit/{id}")
    public String updateComment(@PathVariable Long id,
                                @RequestParam("authorName") String authorName,
                                @RequestParam("email") String email,
                                @RequestParam("content") String content) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");

        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (isAuthenticated) {
            String userEmail = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            boolean isPostAuthor = comment.getPost().getAuthorEmail().equals(userEmail);

            if (!isAdmin && !isPostAuthor) {
                throw new AccessDeniedException("You don't have permission to edit this comment");
            }
        }

        comment.setAuthorName(authorName);
        comment.setEmail(email);
        comment.setContent(content);
        commentService.save(comment);

        return "redirect:/post/" + comment.getPost().getId();
    }

    @GetMapping("/comment/delete/{id}")
    public String deleteComment(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");

        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (isAuthenticated) {
            String userEmail = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            boolean isPostAuthor = comment.getPost().getAuthorEmail().equals(userEmail);

            if (!isAdmin && !isPostAuthor) {
                throw new AccessDeniedException("You don't have permission to delete this comment");
            }
        }

        Long postId = comment.getPost().getId();
        commentService.deleteById(id);
        return "redirect:/post/" + postId;
    }
}
