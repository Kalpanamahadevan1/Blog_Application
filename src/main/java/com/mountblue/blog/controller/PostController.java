package com.mountblue.blog.controller;

import com.mountblue.blog.entity.Post;
import com.mountblue.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/")
    public String index(
            Model model,
            @RequestParam(value = "selectedAuthors",required = false) Set<String> selectedAuthors,
            @RequestParam(value = "selectedTags" ,required = false) Set<String> selectedTags,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "titleSearch", required = false) String titleSearch,
            @RequestParam(value = "sortField", required = false, defaultValue = "publishedAt") String sortField,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page
    ) {

        Page<Post> postsPage;
        int pageSize = 10;
        if (selectedAuthors == null) selectedAuthors = new TreeSet<>();
        if (selectedTags == null) selectedTags = new TreeSet<>();

        if (titleSearch != null && !titleSearch.isEmpty()) {
            postsPage = postService.searchByTitle(titleSearch, page, pageSize);
        } else {
            String sortString = sortField + sortOrder;
            postsPage = postService.findPosts(selectedAuthors, selectedTags, sortString, page, pageSize);
        }

        List<Post> posts = postsPage.getContent();
        Set<String> all_authors = new TreeSet<>();
        List<Post> allPost = postService.findAll();
        Set<String> alltags = new TreeSet<>();
        for(int i=0;i<=allPost.size()-1;i++){
            all_authors.add(allPost.get(i).getAuthorName());
            Set<String> set = allPost.get(i).getTags();
            Iterator it = set.iterator();
            while(it.hasNext()){
                alltags.add((String) it.next());
            }
        }
        posts.forEach(post -> post.setTagsDisplay(String.join(", ", post.getTags())));

        model.addAttribute("posts", posts);
        model.addAttribute("all_authors",all_authors);
        model.addAttribute("all_tags",alltags);
        model.addAttribute("author", author);
        model.addAttribute("tag", tag);
        model.addAttribute("sortField", sortField); // pass to view to keep dropdown selected
        model.addAttribute("sortOrder", sortOrder); // pass to view to keep dropdown selected
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("titleSearch", titleSearch);

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
