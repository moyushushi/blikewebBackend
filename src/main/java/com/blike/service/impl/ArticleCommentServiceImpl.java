package com.blike.service.impl;

import com.blike.entity.ArticleComment;
import com.blike.mapper.ArticleCommentMapper;
import com.blike.service.ArticleCommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleCommentServiceImpl implements ArticleCommentService {

    @Resource
    private ArticleCommentMapper articleCommentMapper;   // 字段类型和变量名必须明确

    @Override
    public void addComment(Integer articleId, Integer userId, String content) {
        ArticleComment comment = new ArticleComment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        articleCommentMapper.insert(comment);
        // 更新文章评论计数
        articleCommentMapper.updateCommentCount(articleId, 1);
    }

    @Override
    public List<ArticleComment> getCommentsByArticleId(Integer articleId) {
        return articleCommentMapper.selectByArticleId(articleId);
    }
}