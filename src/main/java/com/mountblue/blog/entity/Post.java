package com.mountblue.blog.entity;

import com.mountblue.blog.entity.Comment;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String excerpt;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String authorName;

    private LocalDateTime publishedAt;

    @ElementCollection
    @CollectionTable(name = "tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "name")
    private Set<String> tags = new HashSet<>();

    @Transient
    private String tagsDisplay;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public Set<String> getTags() {return tags;}
    public void setTags(Set<String> tags) {this.tags = tags;}

    public String getTagsDisplay() { return tagsDisplay; }
    public void setTagsDisplay(String tagsDisplay) { this.tagsDisplay = tagsDisplay; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
}
