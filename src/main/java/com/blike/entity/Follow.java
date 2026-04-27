package com.blike.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Follow implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer followerId;
    private Integer followingId;
    private LocalDateTime createTime;
}