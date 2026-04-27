package com.blike.utils;

import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;


/**
 * 高并发计数工具（播放量/点赞/评论数）
 * 核心：先写Redis，定时刷回MySQL
 */
@Slf4j
@Data
@Component
public class RedisCountUtil {


    @Resource
    private StringRedisTemplate stringRedisTemplate;


    // Redis Key 前缀定义
    private static final String VIDEO_PLAY_COUNT = "video:play:count:";      // 播放量
    private static final String VIDEO_LIKE_COUNT = "video:like:count:";      // 点赞数
    private static final String VIDEO_COMMENT_COUNT = "video:comment:count:";
    private static final String ARTICLE_VIEW_COUNT = "article:view:count:";
    private static final String ARTICLE_LIKE_COUNT = "article:like:count:";
    private static final String ARTICLE_COMMENT_COUNT = "article:comment:count:";

    /**
     * 评论数 +1/-1
     */
    // 修改所有计数方法，使用 stringRedisTemplate
    public void incrementPlayCount(Integer videoId) {
        String key = VIDEO_PLAY_COUNT + videoId;
        Long newValue = stringRedisTemplate.opsForValue().increment(key, 1);
        log.info("播放量计数: key={}, 新值={}", key, newValue);
        if (newValue != null && newValue == 1L) {
            stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }

    public void incrementLikeCount(Integer videoId, int delta) {
        String key = VIDEO_LIKE_COUNT + videoId;
        Long newValue = stringRedisTemplate.opsForValue().increment(key, delta);
        log.info("点赞计数: key={}, delta={}, 新值={}", key, delta, newValue);
        if (newValue == null) {
            log.error("Redis increment 返回 null，操作失败！");
            return;
        }
        if (newValue == 1L) {
            stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }

    public void incrementCommentCount(Integer videoId, int delta) {
        String key = VIDEO_COMMENT_COUNT + videoId;
        Long newValue = stringRedisTemplate.opsForValue().increment(key, delta);
        log.info("评论计数: key={}, delta={}, 新值={}", key, delta, newValue);
        if (newValue != null && newValue == 1L) {
            stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }

    // 修改 getCount 方法，使用 stringRedisTemplate
    public Long getCount(String keyPrefix, Integer videoId) {
        String key = keyPrefix + videoId;
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) return 0L;
            return Long.parseLong(value);
        } catch (Exception e) {
            log.error("读取计数失败，key={}", key, e);
            stringRedisTemplate.delete(key);
            return 0L;
        }
    }
    public Long getPlayCount(Integer videoId) {
        return getCount(VIDEO_PLAY_COUNT, videoId);
    }

    public Long getLikeCount(Integer videoId) {
        return getCount(VIDEO_LIKE_COUNT, videoId);
    }

    public Long getCommentCount(Integer videoId) {
        return getCount(VIDEO_COMMENT_COUNT, videoId);
    }
    public void incrementArticleViewCount(Integer articleId) {
        String key = ARTICLE_VIEW_COUNT + articleId;
        stringRedisTemplate.opsForValue().increment(key, 1);
        if (!stringRedisTemplate.hasKey(key)) {
            stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }

    public void incrementArticleLikeCount(Integer articleId, int delta) {
        String key = ARTICLE_LIKE_COUNT + articleId;
        stringRedisTemplate.opsForValue().increment(key, delta);
        if (!stringRedisTemplate.hasKey(key)) {
            stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }

    public void incrementArticleCommentCount(Integer articleId, int delta) {
        String key = ARTICLE_COMMENT_COUNT + articleId;
        stringRedisTemplate.opsForValue().increment(key, delta);
        if (!stringRedisTemplate.hasKey(key)) {
            stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }
    // 获取实时计数的快捷方法
    public Long getArticleViewCount(Integer articleId) {
        return getCount(ARTICLE_VIEW_COUNT, articleId);
    }
    public Long getArticleLikeCount(Integer articleId) {
        return getCount(ARTICLE_LIKE_COUNT, articleId);
    }
    public Long getArticleCommentCount(Integer articleId) {
        return getCount(ARTICLE_COMMENT_COUNT, articleId);
    }
}