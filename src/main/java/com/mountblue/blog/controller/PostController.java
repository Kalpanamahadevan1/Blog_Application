package com.mountblue.blog.controller;

import com.mountblue.blog.entity.Post;
import com.mountblue.blog.entity.User;
import com.mountblue.blog.service.PostService;
import com.mountblue.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(
            Model model,
            @RequestParam(value = "selectedAuthors", required = false) Set<String> selectedAuthors,
            @RequestParam(value = "selectedTags", required = false) Set<String> selectedTags,
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "sortField", required = false, defaultValue = "publishedAt") String sortField,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page
    ) {

        Page<Post> postsPage;
        int pageSize = 10;

        if (selectedAuthors == null) selectedAuthors = new TreeSet<>();
        if (selectedTags == null) selectedTags = new TreeSet<>();

        String sortString = sortField + sortOrder.substring(0, 1).toUpperCase() + sortOrder.substring(1);

        if (searchText != null && !searchText.trim().isEmpty()) {
            postsPage = postService.fullTextSearch(searchText.trim(), page, pageSize, sortString);
        } else {
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

            postsPage = postService.findPosts(selectedAuthors, selectedTags, startDateTime, endDateTime, sortString, page, pageSize);
        }

        List<Post> posts = postsPage.getContent();

        Set<String> all_authors = new TreeSet<>();
        Set<String> alltags = new TreeSet<>();
        List<Post> allPost = postService.findAll();

        for (Post post : allPost) {
            all_authors.add(post.getAuthorName());
            alltags.addAll(post.getTags());
        }

        posts.forEach(post -> post.setTagsDisplay(String.join(", ", post.getTags())));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        String currentUserEmail = isAuthenticated ? auth.getName() : null;
        boolean isAdmin = isAuthenticated && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("posts", posts);
        model.addAttribute("all_authors", all_authors);
        model.addAttribute("all_tags", alltags);
        model.addAttribute("searchText", searchText);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("currentUserEmail", currentUserEmail);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("selectedAuthors", selectedAuthors);
        model.addAttribute("selectedTags", selectedTags);

        return "index";
    }

    @GetMapping("/post/create")
    public String createPostForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userService.findByEmail(userEmail).orElse(null);

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("post", new Post());
        model.addAttribute("currentUser", user);
        model.addAttribute("isAdmin", isAdmin);
        return "create_post";
    }

    @PostMapping("/post/create")
    public String createPost(@ModelAttribute Post post,
                             @RequestParam("tagsInput") String tagsInput,
                             @RequestParam(value = "authorNameInput", required = false) String authorNameInput) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userService.findByEmail(userEmail).orElse(null);

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin && authorNameInput != null && !authorNameInput.isEmpty()) {
            post.setAuthorName(authorNameInput);
            post.setAuthorEmail(userEmail);
        } else if (user != null) {
            post.setAuthorName(user.getName());
            post.setAuthorEmail(userEmail);
        }

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!isAdmin && !post.getAuthorEmail().equals(userEmail)) {
            throw new AccessDeniedException("You don't have permission to delete this post");
        }

        postService.deleteById(id);
        return "redirect:/";
    }

    @GetMapping("/post/edit/{id}")
    public String editPostForm(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Post post = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!isAdmin && !post.getAuthorEmail().equals(userEmail)) {
            throw new AccessDeniedException("You don't have permission to edit this post");
        }

        model.addAttribute("post", post);
        model.addAttribute("tagsInput", String.join(", ", post.getTags()));
        model.addAttribute("isAdmin", isAdmin);
        return "edit_post";
    }

    @PostMapping("/post/edit/{id}")
    public String updatePost(@PathVariable Long id,
                             @ModelAttribute Post post,
                             @RequestParam("tagsInput") String tagsInput,
                             @RequestParam(value = "authorNameInput", required = false) String authorNameInput) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Post existingPost = postService.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!isAdmin && !existingPost.getAuthorEmail().equals(userEmail)) {
            throw new AccessDeniedException("You don't have permission to edit this post");
        }

        existingPost.setTitle(post.getTitle());
        existingPost.setExcerpt(post.getExcerpt());
        existingPost.setContent(post.getContent());

        if (isAdmin && authorNameInput != null && !authorNameInput.isEmpty()) {
            existingPost.setAuthorName(authorNameInput);
        }

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        String currentUserEmail = isAuthenticated ? auth.getName() : null;
        boolean isAdmin = isAuthenticated && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("post", post);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("currentUserEmail", currentUserEmail);
        model.addAttribute("isAdmin", isAdmin);
        return "view_post";
    }
}