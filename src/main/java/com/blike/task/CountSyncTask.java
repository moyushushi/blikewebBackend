package com.blike.task;

import com.blike.mapper.VideoMapper;
import com.blike.utils.RedisCountUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Set;


/* * 定时任务：将Redis中的计数（播放量/点赞数）定期同步回MySQL
 * 每5分钟将Redis计数刷回MySQL（核心：削峰填谷）
 */
@Slf4j
@Component
public class CountSyncTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private VideoMapper videoMapper;
    @Resource
    private RedisCountUtil redisCountUtil;

    // Redis Key前缀
    private static final String VIDEO_PLAY_COUNT = "video:play:count:";
    private static final String VIDEO_LIKE_COUNT = "video:like:count:";
    private static final String VIDEO_COMMENT_COUNT = "video:comment:count:";
    /**
     * 每5分钟执行一次（可根据业务调整）
            */
    @Scheduled(cron = "0 */1 * * * ?")
    public void syncCountToMysql() {
        log.info("========== 开始同步Redis计数到MySQL ==========");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 同步播放量
            log.info(">>> 开始同步播放量");
            syncCount(VIDEO_PLAY_COUNT, "playCount");
            log.info("<<< 播放量同步完成");

            // 2. 同步点赞数
            log.info(">>> 开始同步点赞数");
            syncCount(VIDEO_LIKE_COUNT, "likeCount");
            log.info("<<< 点赞数同步完成");

            // 3. 同步评论数
            log.info(">>> 开始同步评论数");
            syncCount(VIDEO_COMMENT_COUNT, "commentCount");
            log.info("<<< 评论数同步完成");

        } catch (Exception e) {
            log.error("同步计数过程中发生异常", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("========== 同步结束，耗时: {} ms ==========", (endTime - startTime));
    }

    private void syncCount(String keyPrefix, String field) {
        Set<String> keys = stringRedisTemplate.keys(keyPrefix + "*");
        if (keys.isEmpty()) {
            log.info("未找到需要同步的键，keyPrefix={}", keyPrefix);
            return;
        }

        log.info("找到 {} 个键需要同步，keyPrefix={}", keys.size(), keyPrefix);
        int successCount = 0;
        int failCount = 0;

        for (String key : keys) {
            try {
                Integer videoId = Integer.parseInt(key.replace(keyPrefix, ""));
                // 原子操作：获取旧值并立即置为 "0"
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

                log.info("同步 key={}, videoId={}, 增量={}", key, videoId, count);
                int rows = 0;
                if ("playCount".equals(field)) {
                    rows = videoMapper.UpdatePlayCount(videoId, count);
                } else if ("likeCount".equals(field)) {
                    rows = videoMapper.UpdateLikeCount(videoId, count);
                } else if ("commentCount".equals(field)) {
                    rows = videoMapper.UpdateCommentCount(videoId, count);
                }

                if (rows > 0) {
                    successCount++;
                    log.info("同步成功: videoId={}, field={}, 增量={}, 影响行数={}", videoId, field, count, rows);
                } else {
                    failCount++;
                    log.warn("同步失败（影响行数为0）: videoId={}, field={}, 增量={}", videoId, field, count);
                }

            } catch (Exception e) {
                failCount++;
                log.error("同步键 {} 时发生异常", key, e);
            }
        }
        log.info("同步完成，keyPrefix={}, 成功数={}, 失败数={}", keyPrefix, successCount, failCount);
    }
}