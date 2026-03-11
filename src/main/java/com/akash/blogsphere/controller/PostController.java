package com.akash.blogsphere.controller;

import com.akash.blogsphere.dto.PostRequest;
import com.akash.blogsphere.entity.Post;
import com.akash.blogsphere.entity.User;
import com.akash.blogsphere.repository.PostRepository;
import com.akash.blogsphere.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return ResponseEntity.ok(post);
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody PostRequest request, Authentication authentication) {
        User author = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .build();

        return ResponseEntity.ok(postRepository.save(post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest request, Authentication authentication) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!post.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Unauthorized to edit this post");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        return ResponseEntity.ok(postRepository.save(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Authentication authentication) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!post.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Unauthorized to delete this post");
        }

        postRepository.delete(post);
        return ResponseEntity.ok("Post deleted successfully");
    }
}
