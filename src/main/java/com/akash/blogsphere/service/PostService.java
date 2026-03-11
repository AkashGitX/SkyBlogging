package com.akash.blogsphere.service;

import com.akash.blogsphere.entity.Post;
import com.akash.blogsphere.entity.User;
import com.akash.blogsphere.repository.PostRepository;
import com.akash.blogsphere.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post createPost(String title, String content, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();

        return postRepository.save(post);
    }

    public Post updatePost(Long id, String title, String content) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setTitle(title);
        post.setContent(content);

        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}
