package com.blike.controller;

import com.blike.entity.Article;
import com.blike.entity.ArticleComment;
import com.blike.entity.RestBean;
import com.blike.service.ArticleCommentService;
import com.blike.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // 文章列表
    @GetMapping("/list")
    public RestBean<List<Article>> getArticleList() {
        return RestBean.success(articleService.getArticleList());
    }

    // 文章详情
    @GetMapping("/{id}")
    public RestBean<Article> getArticleDetail(@PathVariable Integer id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return RestBean.failure(404, "文章不存在");
        }
        // 增加阅读数（异步或直接更新）
        articleService.incrementViewCount(id);
        return RestBean.success(article);
    }

    // 发布文章
    @PostMapping("/upload")
    public RestBean<Article> uploadArticle(
            @RequestParam(value = "cover", required = false) MultipartFile coverFile,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            Authentication authentication) {
        String userIdStr = (String) authentication.getPrincipal();
        Integer userId = Integer.parseInt(userIdStr);
        Article article = articleService.publishArticle(coverFile, title, content, category, userId);
        return RestBean.success(article);
    }

    // 点赞/取消点赞（需要 user_like_article 表）
    @PostMapping("/like/{id}")
    public RestBean<String> likeArticle(@PathVariable Integer id,
                                        @RequestParam boolean like,
                                        Authentication authentication) {
        Integer userId = Integer.parseInt((String) authentication.getPrincipal());
        articleService.likeArticle(id, userId, like);
        return RestBean.success(like ? "点赞成功" : "取消点赞");
    }
    @GetMapping("/user/{userId}")
    public RestBean<List<Article>> getArticlesByUser(@PathVariable Integer userId) {
        List<Article> articles = articleService.getArticlesByUserId(userId);
        return RestBean.success(articles);
    }
    @GetMapping("/liked")
    public RestBean<List<Article>> getLikedArticles(Authentication authentication,
                                                    @RequestParam(defaultValue = "10") int limit) {
        Integer userId = Integer.parseInt((String) authentication.getPrincipal());
        List<Article> articles = articleService.getLikedArticles(userId, limit);
        return RestBean.success(articles);
    }
}