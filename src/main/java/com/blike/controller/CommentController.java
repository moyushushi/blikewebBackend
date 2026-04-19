package com.blike.controller;

import com.blike.entity.CommentVO;
import com.blike.entity.RestBean;
import com.blike.entity.ArticleComment;
import com.blike.service.CommentService;
import com.blike.service.ArticleCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService videoCommentService;          // 视频评论服务

    @Autowired
    private ArticleCommentService articleCommentService; // 文章评论服务

    // ========== 视频评论 ==========
    @PostMapping("/video")
    public RestBean<String> addVideoComment(@RequestBody Map<String, String> payload,
                                            Authentication authentication) {
        Integer videoId = Integer.parseInt(payload.get("videoId"));
        String content = payload.get("content");
        Integer userId = Integer.parseInt((String) authentication.getPrincipal());
        videoCommentService.addComment(videoId, userId, content);
        return RestBean.success("评论成功");
    }

    @GetMapping("/video/{videoId}")
    public RestBean<List<CommentVO>> getVideoComments(@PathVariable Integer videoId) {
        List<CommentVO> comments = videoCommentService.getCommentsByVideoId(videoId);
        return RestBean.success(comments);
    }

    // ========== 文章评论 ==========
    @PostMapping("/article")
    public RestBean<String> addArticleComment(@RequestBody Map<String, String> payload,
                                              Authentication authentication) {
        Integer articleId = Integer.parseInt(payload.get("articleId"));
        String content = payload.get("content");
        Integer userId = Integer.parseInt((String) authentication.getPrincipal());
        articleCommentService.addComment(articleId, userId, content);
        return RestBean.success("评论成功");
    }

    @GetMapping("/article/{articleId}")
    public RestBean<List<ArticleComment>> getArticleComments(@PathVariable Integer articleId) {
        List<ArticleComment> comments = articleCommentService.getCommentsByArticleId(articleId);
        return RestBean.success(comments);
    }
}