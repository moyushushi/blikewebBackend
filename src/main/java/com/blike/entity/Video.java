package com.blike.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Video implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;         // 视频ID
    private String title;       // 视频标题
    private String cover;       // 封面图片路径（相对路径，如：/upload/cover/xxx.jpg）
    private String url;         // 视频路径（相对路径，如：/upload/video/xxx.mp4）
    private String author;      // 作者名称
    private String avatar;      // 作者头像路径（相对路径）
    private Integer playCount;   // 播放量
    private String time;        // 视频时长（如：12:30）
    private String desc;        // 视频描述
    private String publishTime; // 发布时间（如：2024-05-01）
    private Integer likeCount;   // 点赞数
    private Integer commentCount;// 评论数
    private String category;  // 视频分类
    private Integer userId;  // 上传者ID
}
