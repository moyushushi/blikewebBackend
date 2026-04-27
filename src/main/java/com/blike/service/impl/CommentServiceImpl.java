package com.blike.service.impl;

import com.blike.entity.Comment;
import com.blike.entity.CommentVO;
import com.blike.mapper.CommentMapper;
import com.blike.mapper.VideoMapper;
import com.blike.service.CommentService;
import com.blike.utils.RedisCountUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CommentServiceImpl implements CommentService {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisCountUtil redisCountUtil;

    // 评论列表缓存Key
    private static final String COMMENT_LIST_KEY = "comment:list:video:";
    // 缓存过期时间：10分钟（可调整）
    private static final long CACHE_EXPIRE_TIME = 10;

    @Override
    public void addComment(Integer videoId, Integer userId, String content) {
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());   // 添加这行
        commentMapper.insert(comment);
        try {
            redisCountUtil.incrementCommentCount(videoId, 1);
        } catch (Exception e) {
            // 即使 Redis 更新失败，也仅记录日志，后续定时任务或全量校准会修复
            System.out.println(e.getMessage());
        }
        // 3. 使该视频的评论列表缓存失效（保证数据一致性）
        String cacheKey = COMMENT_LIST_KEY + videoId;
        redisTemplate.delete(cacheKey);
        // 同时删除可能存在的空值缓存
        redisTemplate.delete(cacheKey + ":empty");
    }

    @Override
    public List<CommentVO> getCommentsByVideoId(Integer videoId) {
        String cacheKey = COMMENT_LIST_KEY + videoId;
        // 1. 先查Redis缓存
        List<CommentVO> commentList = (List<CommentVO>) redisTemplate.opsForValue().get(cacheKey);
        if (commentList != null) {
            return commentList;
        }

        // 2. 缓存穿透防护：缓存空值
        if (redisTemplate.hasKey(cacheKey + ":empty")) {
            return List.of();
        }

        // 3. 查DB
        commentList = commentMapper.selectByVideoIdWithUser(videoId);
        if (commentList == null || commentList.isEmpty()) {
            // 缓存空值，过期时间短一点（5分钟）
            redisTemplate.opsForValue().set(cacheKey + ":empty", "empty", 5, TimeUnit.MINUTES);
            return List.of();
        }

        // 4. 写入Redis缓存（打散过期时间，防止雪崩）
        long expireTime = CACHE_EXPIRE_TIME + (long) (Math.random() * 5); // 10-15分钟随机过期
        redisTemplate.opsForValue().set(cacheKey, commentList, expireTime, TimeUnit.MINUTES);

        return commentList;
    }


}