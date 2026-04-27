package com.blike.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class LikedVideoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String title;
    private String cover;
    private String author;
    private String playCount;
    private String time;
    private LocalDateTime likeTime;  // 点赞时间
}