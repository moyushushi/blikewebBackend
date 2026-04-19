package com.blike.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUserVO {
    private Integer id;          // 被关注用户ID
    private String username;
    private String avatar;
    private LocalDateTime followTime; // 关注时间
}