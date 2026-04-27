package com.blike.task;

import com.blike.mapper.ArticleMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class ArticleCountSyncTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;   // 改为 StringRedisTemplate

    @Resource
    private ArticleMapper articleMapper;

    private static final String ARTICLE_VIEW_COUNT = "article:view:count:";
    private static final String ARTICLE_LIKE_COUNT = "article:like:count:";
    private static final String ARTICLE_COMMENT_COUNT = "article:comment:count:";

    @Scheduled(cron = "0 */5 * * * ?")  // 每5分钟同步一次
    public void syncArticleCounts() {
        log.info("========== 开始同步文章计数到MySQL ==========");
        long start = System.currentTimeMillis();
        syncCount(ARTICLE_VIEW_COUNT, "viewCount");
        syncCount(ARTICLE_LIKE_COUNT, "likeCount");
        syncCount(ARTICLE_COMMENT_COUNT, "commentCount");
        log.info("========== 文章计数同步结束，耗时 {} ms ==========", System.currentTimeMillis() - start);
    }

    private void syncCount(String keyPrefix, String field) {
        Set<String> keys = stringRedisTemplate.keys(keyPrefix + "*");
        if (keys == null || keys.isEmpty()) {
            log.info("未找到需要同步的键，keyPrefix={}", keyPrefix);
            return;
        }

        log.info("找到 {} 个键需要同步，keyPrefix={}", keys.size(), keyPrefix);
        int success = 0, fail = 0;

        for (String key : keys) {
            try {
                Integer articleId = Integer.parseInt(key.replace(keyPrefix, ""));
                // 原子获取旧值并重置为 "0"
                String oldValueStr = stringRedisTemplate.opsForValue().getAndSet(key, "0");
                if (oldValueStr == null) {
                    log.debug("键 {} 的值为 null，跳过", key);
                    continue;
                }
                Long count = Long.parseLong(oldValueStr);
                if (count == 0) {
                    log.debug("键 {} 的增量为0，跳过", key);
                    continue;
                }

                log.info("同步 key={}, articleId={}, 增量={}", key, articleId, count);
                int rows = 0;
                if ("viewCount".equals(field)) {
                    rows = articleMapper.updateViewCount(articleId, count);
                } else if ("likeCount".equals(field)) {
                    rows = articleMapper.updateLikeCount(articleId, count);
                } else if ("commentCount".equals(field)) {
                    rows = articleMapper.updateCommentCount(articleId, count);
                }

                if (rows > 0) {
                    success++;
                    log.info("同步成功: articleId={}, field={}, 增量={}", articleId, field, count);
                } else {
                    fail++;
                    log.warn("同步失败（影响行数为0）: articleId={}, field={}", articleId, field);
                }
            } catch (Exception e) {
                fail++;
                log.error("同步键 {} 时发生异常", key, e);
            }
        }
        log.info("同步完成，keyPrefix={}, 成功数={}, 失败数={}", keyPrefix, success, fail);
    }
}