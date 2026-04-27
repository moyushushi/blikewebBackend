package com.blike.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer videoId;
    private Integer userId;
    private String content;
    private LocalDateTime createTime;
}