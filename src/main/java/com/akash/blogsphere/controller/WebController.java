package com.akash.blogsphere.controller;

import com.akash.blogsphere.entity.Post;
import com.akash.blogsphere.service.CommentService;
import com.akash.blogsphere.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("posts", postService.getAllPosts());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/post/create")
    public String createPost() {
        return "create-post";
    }

    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Optional<Post> post = postService.getPostById(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            model.addAttribute("comments", commentService.getCommentsByPostId(id));
        } else {
            model.addAttribute("error", "Post not found");
        }
        return "post";
    }

    @PostMapping("/post/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             Authentication authentication) {
        if (authentication != null && content != null && !content.trim().isEmpty()) {
            commentService.addComment(id, authentication.getName(), content);
        }
        return "redirect:/post/" + id;
    }
}
