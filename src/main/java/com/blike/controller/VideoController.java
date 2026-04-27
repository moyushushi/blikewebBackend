package com.blike.controller;

import com.blike.entity.Comment;
import com.blike.entity.CommentVO;
import com.blike.entity.RestBean;
import com.blike.entity.Video;
import com.blike.service.CommentService;
import com.blike.service.VideoService;
import com.blike.utils.RedisCountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private RedisCountUtil redisCountUtil;

    @GetMapping("/list")
    public RestBean<List<Video>> getVideoList() {
        try {
            List<Video> videoList = videoService.getVideoList();
            return RestBean.success(videoList);
        } catch (Exception e) {
            return RestBean.failure(500, "获取视频列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public RestBean<Video> getVideoDetail(@PathVariable("id") Integer id) {
        try {
            Video video = videoService.getVideoById(id);
            if (video == null) {
                return RestBean.failure(404, "视频不存在");
            }
            return RestBean.success(video);
        } catch (Exception e) {
            return RestBean.failure(500, "获取视频详情失败：" + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public RestBean<Video> uploadVideo(
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile,
            @RequestParam("title") String title,
            @RequestParam("desc") String desc,
            @RequestParam("category") String category,
            Authentication authentication) {
        try {
            // 从 Authentication 获取 userId（字符串）
            String userIdStr = (String) authentication.getPrincipal();
            Integer userId = Integer.parseInt(userIdStr);
            Video video = videoService.publishVideo(videoFile, coverFile, title, desc, category, userId);
            return RestBean.success(video);
        } catch (Exception e) {
            return RestBean.failure(500, "发布失败：" + e.getMessage());
        }
    }


    @GetMapping("/user/{userId}")
    public RestBean<List<Video>> getVideosByUser(@PathVariable Integer userId) {
        try {
            List<Video> videos = videoService.getVideosByUserId(userId);
            return RestBean.success(videos);
        } catch (Exception e) {
            return RestBean.failure(500, "获取用户视频失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/play")
    public RestBean<String> incrementPlayCount(@PathVariable("id") Integer id) {
        try {
            // 核心改这里：调用Redis计数，不再直接更新数据库
            redisCountUtil.incrementPlayCount(id);
            return RestBean.success("播放量+1（Redis）");
        } catch (Exception e) {
            return RestBean.failure(500, "操作失败：" + e.getMessage());
        }
    }

}