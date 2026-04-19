package com.blike.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Integer id;
    private Integer videoId;
    private Integer userId;
    private String content;
    private LocalDateTime createTime;
}