package com.akash.blogsphere.service;

import com.akash.blogsphere.entity.Comment;
import com.akash.blogsphere.entity.Post;
import com.akash.blogsphere.entity.User;
import com.akash.blogsphere.repository.CommentRepository;
import com.akash.blogsphere.repository.PostRepository;
import com.akash.blogsphere.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    public Comment addComment(Long postId, String userEmail, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .post(post)
                .build();

        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
