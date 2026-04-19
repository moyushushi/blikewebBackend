package com.blike.service.impl;

import com.blike.entity.Article;
import com.blike.entity.LikedArticleVO;
import com.blike.mapper.ArticleMapper;
import com.blike.mapper.LikeArticleMapper;
import com.blike.service.ArticleService;
import com.blike.service.VideoService; // 复用文件上传
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private VideoService videoService; // 复用 uploadCover 方法

    @Resource
    private LikeArticleMapper likeArticleMapper;

    @Override
    public List<Article> getArticleList() {
        return articleMapper.selectAllPublished();
    }

    @Override
    public Article getArticleById(Integer id) {
        return articleMapper.selectById(id);
    }

    @Override
    public Article publishArticle(MultipartFile coverFile, String title, String content, String category, Integer userId) {
        // 处理封面
        String coverUrl = (coverFile != null && !coverFile.isEmpty())
                ? videoService.uploadCover(coverFile)
                : "/upload/cover/default.jpg";

        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setCover(coverUrl);
        article.setCategory(category);
        article.setUserId(userId);
        article.setStatus(1); // 已发布
        article.setPublishTime(LocalDateTime.now());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);

        articleMapper.insert(article);
        return article;
    }

    @Override
    public void incrementViewCount(Integer id) {
        articleMapper.incrementViewCount(id);
    }

    @Override
    public void likeArticle(Integer articleId, Integer userId, boolean like) {
        if (like) {
            // 检查是否已点赞
            if (likeArticleMapper.exists(userId, articleId)) {
                throw new RuntimeException("已经点过赞");
            }
            LikedArticleVO record = new LikedArticleVO();
            record.setUserId(userId);
            record.setArticleId(articleId);
            likeArticleMapper.insert(record);
            articleMapper.updateLikeCount(articleId, 1);
        } else {
            likeArticleMapper.delete(userId, articleId);
            articleMapper.updateLikeCount(articleId, -1);
        }
    }

    @Override
    public List<Article> getArticlesByUserId(Integer userId) {
        return articleMapper.selectByUserId(userId);
    }

    @Override
    public List<Article> getLikedArticles(Integer userId, int limit) {
        return likeArticleMapper.findLikedArticlesWithDetail(userId, limit);
    }

}