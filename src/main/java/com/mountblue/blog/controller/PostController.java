package com.mountblue.blog.controller;

import com.mountblue.blog.entity.Post;
import com.mountblue.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class PostController {

    @Autowired
    private PostService postService;

    // sorting asc and desc
@GetMapping("/")
public String index(
        Model model,
        @RequestParam(value = "author", required = false) String author,
        @RequestParam(value = "tag", required = false) String tag,
        @RequestParam(value = "sortField", required = false, defaultValue = "publishedAt") String sortField,
        @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder, // <-- new
        @RequestParam(value = "page", required = false, defaultValue = "0") int page
) {
    String sortString = sortField + sortOrder; // combine field + order, e.g., "titleasc"
    Page<Post> postsPage = postService.findPosts(author, tag, sortString, page, 10);

    List<Post> posts = postsPage.getContent();
    posts.forEach(post -> post.setTagsDisplay(String.join(", ", post.getTags())));

    model.addAttribute("posts", posts);
    model.addAttribute("author", author);
    model.addAttribute("tag", tag);
    model.addAttribute("sortField", sortField); // pass to view to keep dropdown selected
    model.addAttribute("sortOrder", sortOrder); // pass to view to keep dropdown selected
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", postsPage.getTotalPages());

    return "index";
}

    @GetMapping("/post/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create_post";
    }

    @PostMapping("/post/create")
    public String createPost(@ModelAttribute Post post,
                             @RequestParam("tagsInput") String tagsInput) {
        post.setPublishedAt(LocalDateTime.now());
        Set<String> tags = new HashSet<>();
        if (tagsInput != null && !tagsInput.isEmpty()) {
            for (String tag : tagsInput.split(",")) tags.add(tag.trim());
        }
        post.setTags(tags);
        postService.save(post);
        return "redirect:/";
    }

    @GetMapping("/post/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        postService.deleteById(id);
        return "redirect:/";
    }

    @GetMapping("/post/edit/{id}")
    public String editPostForm(@PathVariable Long id, Model model) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        model.addAttribute("post", post);
        model.addAttribute("tagsInput", String.join(", ", post.getTags()));
        return "edit_post";
    }

    @PostMapping("/post/edit/{id}")
    public String updatePost(@PathVariable Long id,
                             @ModelAttribute Post post,
                             @RequestParam("tagsInput") String tagsInput) {
        Post existingPost = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        existingPost.setTitle(post.getTitle());
        existingPost.setExcerpt(post.getExcerpt());
        existingPost.setContent(post.getContent());
        existingPost.setAuthorName(post.getAuthorName());

        Set<String> tags = new HashSet<>();
        if (tagsInput != null && !tagsInput.isEmpty()) {
            for (String tag : tagsInput.split(",")) tags.add(tag.trim());
        }
        existingPost.setTags(tags);

        postService.save(existingPost);
        return "redirect:/";
    }

    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setTagsDisplay(String.join(", ", post.getTags()));
        model.addAttribute("post", post);
        return "view_post";
    }
}
