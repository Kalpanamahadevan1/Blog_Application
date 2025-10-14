package com.mountblue.blog.service;

import com.mountblue.blog.entity.Post;
import com.mountblue.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }

    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    public Page<Post> getPosts(String author, String tag, int page, String sort) {
        Sort sorting = sort.equals("title") ? Sort.by("title").ascending() : Sort.by("publishedAt").descending();
        Pageable pageable = PageRequest.of(page, 10, sorting); // 10 per page
        return postRepository.findByAuthorNameAndTags(
                author == null ? "" : author,
                tag == null ? "" : tag,
                pageable
        );
    }

    // sort asc and desc
    public Page<Post> findPosts(String author, String tag, String sortField, int page, int size) {
        Sort sort;

        if ("titleAsc".equalsIgnoreCase(sortField)) {
            sort = Sort.by("title").ascending();
        } else if ("titleDesc".equalsIgnoreCase(sortField)) {
            sort = Sort.by("title").descending();
        } else if ("publishedAtAsc".equalsIgnoreCase(sortField)) {
            sort = Sort.by("publishedAt").ascending();
        } else if ("publishedAtDesc".equalsIgnoreCase(sortField)) {  // fix here
            sort = Sort.by("publishedAt").descending();
        } else {
            sort = Sort.by("publishedAt").descending();  // default
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        if ((author == null || author.isEmpty()) && (tag == null || tag.isEmpty())) {
            return postRepository.findAll(pageable);
        } else if (author != null && !author.isEmpty() && (tag == null || tag.isEmpty())) {
            return postRepository.findByAuthorName(author, pageable);
        } else if ((author == null || author.isEmpty()) && tag != null && !tag.isEmpty()) {
            return postRepository.findByTags(tag, pageable);
        } else {
            return postRepository.findByAuthorNameContainingIgnoreCaseAndTagsContaining(author, tag, pageable);
        }
    }
}

