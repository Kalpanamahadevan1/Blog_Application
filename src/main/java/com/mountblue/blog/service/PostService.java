package com.mountblue.blog.service;

import com.mountblue.blog.entity.Post;
import com.mountblue.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

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

public Page<Post> findPosts(Set<String> authors, Set<String> tags,
                            LocalDateTime startDate, LocalDateTime endDate,
                            String sortString, int page, int size) {

    Pageable pageable = PageRequest.of(page, size);

    if (authors == null) authors = Collections.emptySet();
    if (tags == null) tags = Collections.emptySet();

    String startDateStr = startDate != null ? startDate.toString() : null;
    String endDateStr = endDate != null ? endDate.toString() : null;

    if (authors.isEmpty() && tags.isEmpty() && startDateStr == null && endDateStr == null) {
        Sort sort = parseSortString(sortString);
        pageable = PageRequest.of(page, size, sort);
        return postRepository.findAll(pageable);
    }

    return postRepository.findWithFilters(authors, tags, startDateStr, endDateStr, pageable);
}

    public Page<Post> fullTextSearch(String searchText, int page, int pageSize, String sortString) {
        Sort sort = parseSortString(sortString);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        return postRepository.fullTextSearch(searchText, pageable);
    }

    private Sort parseSortString(String sortString) {
        if ("titleAsc".equalsIgnoreCase(sortString)) {
            return Sort.by("title").ascending();
        } else if ("titleDesc".equalsIgnoreCase(sortString)) {
            return Sort.by("title").descending();
        } else if ("publishedAtAsc".equalsIgnoreCase(sortString)) {
            return Sort.by("publishedAt").ascending();
        } else {
            return Sort.by("publishedAt").descending();
        }
    }
}