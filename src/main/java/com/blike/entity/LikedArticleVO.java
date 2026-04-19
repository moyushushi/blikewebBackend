package com.blike.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikedArticleVO {
    private Integer id;
    private Integer userId;
    private Integer articleId;
    private LocalDateTime createTime;
}
