package com.blike.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String title;
    private String content;
    private String cover;
    private String category;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime publishTime;
    private Integer userId;
    private Integer status;
    // 关联字段（查询时从account表填充）
    private String author;
    private String avatar;
}