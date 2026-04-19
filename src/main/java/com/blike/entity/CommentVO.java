package com.blike.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Integer id;
    private Integer videoId;
    private Integer userId;
    private String content;
    private LocalDateTime createTime;   // 使用 LocalDateTime 与数据库匹配
    private String username;            // 评论者用户名
    private String avatar;              // 评论者头像路径
}