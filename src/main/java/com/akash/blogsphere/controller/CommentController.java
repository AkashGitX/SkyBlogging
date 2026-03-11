package com.akash.blogsphere.controller;

import com.akash.blogsphere.dto.CommentRequest;
import com.akash.blogsphere.entity.Comment;
import com.akash.blogsphere.entity.Post;
import com.akash.blogsphere.entity.User;
import com.akash.blogsphere.repository.CommentRepository;
import com.akash.blogsphere.repository.PostRepository;
import com.akash.blogsphere.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/posts/{postId}/comments")
    public List<Comment> getCommentsByPostId(@PathVariable Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Comment> createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequest request, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .post(post)
                .build();

        return ResponseEntity.ok(commentRepository.save(comment));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, Authentication authentication) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!comment.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Unauthorized to delete this comment");
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
    }
}
