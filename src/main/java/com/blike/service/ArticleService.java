package com.blike.service;

import com.blike.entity.Article;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {
    List<Article> getArticleList();
    Article getArticleById(Integer id);
    Article publishArticle(MultipartFile coverFile, String title, String content, String category, Integer userId);
    void incrementViewCount(Integer id);
    void likeArticle(Integer articleId, Integer userId, boolean like);
    // 评论相关由 CommentService 处理
    List<Article> getArticlesByUserId(Integer userId);

    List<Article> getLikedArticles(Integer userId, int limit);
}