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
import java.util.Set;

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
        return postRepository.findByAuthorNameContainingIgnoreCaseAndTagsContainingIgnoreCase(
                author == null ? "" : author,
                tag == null ? "" : tag,
                pageable
        );
    }

    public Page<Post> findPosts(Set<String> author, Set<String> tag, String sortField, int page, int size) {
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
        boolean authorsEmpty = (author == null || author.isEmpty());
        boolean tagsEmpty = (tag == null || tag.isEmpty());

        if (authorsEmpty && tagsEmpty) {
            return postRepository.findAll(pageable);
        } else if (!authorsEmpty && tagsEmpty) {
            return postRepository.findByAuthorNameIn(author, pageable);
        } else if (authorsEmpty && !tagsEmpty) {
            return postRepository.findByTagsIn(tag, pageable);
        } else {
            return postRepository.findByAuthorNameInAndTagsIn(author, tag, pageable);
        }
    }

    public Page<Post> searchByTitle(String titleSearch, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return postRepository.findByTitleContainingIgnoreCase(titleSearch, pageable);
    }
}

