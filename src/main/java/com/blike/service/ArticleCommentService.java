package com.blike.service;

import com.blike.entity.ArticleComment;
import java.util.List;

public interface ArticleCommentService {
    void addComment(Integer articleId, Integer userId, String content);
    List<ArticleComment> getCommentsByArticleId(Integer articleId);
}