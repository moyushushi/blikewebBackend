package com.blike.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleComment {
    private Integer id;
    private Integer articleId;
    private Integer userId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
    // 关联用户信息（可选）
    private String username;
    private String userAvatar;
}