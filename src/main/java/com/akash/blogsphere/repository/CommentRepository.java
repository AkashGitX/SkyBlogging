package com.akash.blogsphere.repository;

import com.akash.blogsphere.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);
}
