package com.blike.service;

import com.blike.entity.Comment;
import com.blike.entity.CommentVO;

import java.util.List;

public interface CommentService {
    void addComment(Integer videoId, Integer userId, String content);
    List<CommentVO> getCommentsByVideoId(Integer videoId);
}
