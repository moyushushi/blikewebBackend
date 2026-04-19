package com.blike.service.impl;

import com.blike.entity.Comment;
import com.blike.entity.CommentVO;
import com.blike.mapper.CommentMapper;
import com.blike.mapper.VideoMapper;
import com.blike.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private VideoMapper videoMapper;

    @Override
    public void addComment(Integer videoId, Integer userId, String content) {
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());   // 添加这行
        commentMapper.insert(comment);
        videoMapper.incrementCommentCount(videoId);
    }

    @Override
    public List<CommentVO> getCommentsByVideoId(Integer videoId) {
        return commentMapper.selectByVideoIdWithUser(videoId);
    }
}