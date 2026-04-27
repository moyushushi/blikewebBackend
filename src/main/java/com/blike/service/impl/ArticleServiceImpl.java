package com.blike.service.impl;

import com.blike.entity.Article;
import com.blike.entity.LikedArticleVO;
import com.blike.mapper.ArticleMapper;
import com.blike.mapper.LikeArticleMapper;
import com.blike.service.ArticleService;
import com.blike.service.VideoService; // 复用文件上传
import com.blike.utils.RedisCountUtil;
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

    @Resource
    private RedisCountUtil redisCountUtil;

    @Override
    public List<Article> getArticleList() {
        List<Article> list = articleMapper.selectAllPublished();
        for (Article article : list) {
            long viewInc = redisCountUtil.getArticleViewCount(article.getId());
            long likeInc = redisCountUtil.getArticleLikeCount(article.getId());
            long commentInc = redisCountUtil.getArticleCommentCount(article.getId());
            article.setViewCount(article.getViewCount() + (int) viewInc);
            article.setLikeCount(article.getLikeCount() + (int) likeInc);
            article.setCommentCount(article.getCommentCount() + (int) commentInc);
        }
        return list;
    }

    @Override
    public Article getArticleById(Integer id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            long viewInc = redisCountUtil.getArticleViewCount(id);
            long likeInc = redisCountUtil.getArticleLikeCount(id);
            long commentInc = redisCountUtil.getArticleCommentCount(id);
            article.setViewCount(article.getViewCount() + (int) viewInc);
            article.setLikeCount(article.getLikeCount() + (int) likeInc);
            article.setCommentCount(article.getCommentCount() + (int) commentInc);
        }
        return article;
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
        redisCountUtil.incrementArticleViewCount(id);  // 改为 Redis 计数
    }

    @Override
    public void likeArticle(Integer articleId, Integer userId, boolean like) {
        if (like) {
            if (likeArticleMapper.exists(userId, articleId)) {
                throw new RuntimeException("已经点过赞");
            }
            LikedArticleVO record = new LikedArticleVO();
            record.setUserId(userId);
            record.setArticleId(articleId);
            likeArticleMapper.insert(record);
            redisCountUtil.incrementArticleLikeCount(articleId, 1);   // 改为 Redis
        } else {
            likeArticleMapper.delete(userId, articleId);
            redisCountUtil.incrementArticleLikeCount(articleId, -1);  // 改为 Redis
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