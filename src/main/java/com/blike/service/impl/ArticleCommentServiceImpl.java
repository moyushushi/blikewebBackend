package com.blike.service.impl;

import com.blike.entity.ArticleComment;
import com.blike.mapper.ArticleCommentMapper;
import com.blike.service.ArticleCommentService;
import com.blike.utils.RedisCountUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleCommentServiceImpl implements ArticleCommentService {

    @Resource
    private ArticleCommentMapper articleCommentMapper;
    @Resource
    private RedisCountUtil redisCountUtil;

    @Override
    public void addComment(Integer articleId, Integer userId, String content) {
        ArticleComment comment = new ArticleComment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        articleCommentMapper.insert(comment);
        // 改为 Redis 计数
        redisCountUtil.incrementArticleCommentCount(articleId, 1);
    }

    @Override
    public List<ArticleComment> getCommentsByArticleId(Integer articleId) {
        return articleCommentMapper.selectByArticleId(articleId);
    }
}