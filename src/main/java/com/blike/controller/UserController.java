package com.blike.controller;

import com.blike.entity.FollowUserVO;
import com.blike.entity.LikedVideoVO;
import com.blike.entity.RestBean;
import com.blike.entity.user.Account;
import com.blike.entity.user.AccountUser;
import com.blike.mapper.UserMapper;
import com.blike.service.FollowService;
import com.blike.service.LikeVideoService;
import com.blike.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private VideoService videoService;

    @Resource
    private FollowService followService;

    @Resource
    private LikeVideoService likeVideoService;

    @GetMapping("/me")
    public RestBean<AccountUser> me(Authentication authentication) {
        String userIdStr = (String) authentication.getPrincipal();
        Integer userId = Integer.parseInt(userIdStr);
        // 使用不含密码的查询
        AccountUser user = userMapper.findAccountUserById(userId);
        return RestBean.success(user);
    }


    @PostMapping("/change-password")
    public RestBean<String> changePassword(@RequestBody Map<String, String> payload,
                                           Authentication authentication) {
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return RestBean.failure(400, "当前密码不能为空");
        }
        if (newPassword == null || newPassword.length() < 3 || newPassword.length() > 14) {
            return RestBean.failure(400, "新密码长度需在3-14位之间");
        }

        String userIdStr = (String) authentication.getPrincipal();
        Integer userId = Integer.parseInt(userIdStr);

        // 使用包含密码的查询
        Account account = userMapper.findAccountById(userId);
        if (account == null) {
            return RestBean.failure(404, "用户不存在");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword, account.getPassword())) {
            return RestBean.failure(400, "当前密码错误");
        }

        String encodedNewPassword = encoder.encode(newPassword);
        int rows = userMapper.updatePassword(userId, encodedNewPassword);
        if (rows > 0) {
            return RestBean.success("密码修改成功");
        } else {
            return RestBean.failure(500, "修改失败，请稍后重试");
        }
    }

    @PostMapping("/upload-avatar")
    public RestBean<String> uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                         Authentication authentication) {
        try {
            String userIdStr = (String) authentication.getPrincipal();
            Integer userId = Integer.parseInt(userIdStr);

            String avatarUrl = videoService.uploadAvatar(file);
            int rows = userMapper.updateAvatar(userId, avatarUrl);
            if (rows > 0) {
                return RestBean.success(avatarUrl);
            } else {
                return RestBean.failure(500, "更新头像失败");
            }
        } catch (Exception e) {
            return RestBean.failure(500, "上传头像失败：" + e.getMessage());
        }
    }
    @GetMapping("/following")
    public RestBean<List<FollowUserVO>> getFollowingList(Authentication authentication) {
        String userIdStr = (String) authentication.getPrincipal();
        Integer userId = Integer.parseInt(userIdStr);
        List<FollowUserVO> list = followService.getFollowingList(userId);
        return RestBean.success(list);
    }

    // 获取点赞视频列表（默认最近10条）
    @GetMapping("/liked-videos")
    public RestBean<List<LikedVideoVO>> getLikedVideos(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            Authentication authentication) {
        String userIdStr = (String) authentication.getPrincipal();
        Integer userId = Integer.parseInt(userIdStr);
        List<LikedVideoVO> list = likeVideoService.getLikedVideos(userId, limit);
        return RestBean.success(list);
    }

    // 可选：关注/取消关注接口（如果需要在前端直接操作）
    @PostMapping("/follow/{followingId}")
    public RestBean<String> follow(@PathVariable Integer followingId, Authentication authentication) {
        String userIdStr = (String) authentication.getPrincipal();
        Integer userId = Integer.parseInt(userIdStr);
        followService.follow(userId, followingId);
        return RestBean.success("关注成功");
    }

    @DeleteMapping("/follow/{followingId}")
    public RestBean<String> unfollow(@PathVariable Integer followingId, Authentication authentication) {
        String userIdStr = (String) authentication.getPrincipal();
        Integer userId = Integer.parseInt(userIdStr);
        followService.unfollow(userId, followingId);
        return RestBean.success("取消关注成功");
    }
    @GetMapping("/is-following")
    public RestBean<Boolean> isFollowing(@RequestParam Integer userId, Authentication authentication) {
        Integer currentUserId = Integer.parseInt((String) authentication.getPrincipal());
        boolean following = followService.isFollowing(currentUserId, userId);
        return RestBean.success(following);
    }
    // 可选：点赞/取消点赞接口
    @PostMapping("/like/{videoId}")
    public RestBean<String> likeVideo(@PathVariable Integer videoId, Authentication authentication) {
        String userIdStr = (String) authentication.getPrincipal();
        Integer userId = Integer.parseInt(userIdStr);
        likeVideoService.likeVideo(userId, videoId);
        return RestBean.success("点赞成功");
    }

    @DeleteMapping("/like/{videoId}")
    public RestBean<String> unlikeVideo(@PathVariable Integer videoId, Authentication authentication) {
        String userIdStr = (String) authentication.getPrincipal();
        Integer userId = Integer.parseInt(userIdStr);
        likeVideoService.unlikeVideo(userId, videoId);
        return RestBean.success("取消点赞成功");
    }
    @GetMapping("/is-liked")
    public RestBean<Boolean> isLiked(@RequestParam Integer videoId, Authentication authentication) {
        Integer userId = Integer.parseInt((String) authentication.getPrincipal());
        boolean liked = likeVideoService.isLiked(userId, videoId);
        return RestBean.success(liked);
    }
    @GetMapping("/info/{userId}")
    public RestBean<AccountUser> getUserInfo(@PathVariable Integer userId) {
        AccountUser user = userMapper.findAccountUserById(userId);
        if (user == null) {
            return RestBean.failure(404, "用户不存在");
        }
        return RestBean.success(user);
    }
    @PostMapping("/update-bio")
    public RestBean<String> updateBio(@RequestBody Map<String, String> payload, Authentication authentication) {
        String bio = payload.get("bio");
        Integer userId = Integer.parseInt((String) authentication.getPrincipal());
        int rows = userMapper.updateBio(userId, bio);
        return rows > 0 ? RestBean.success("更新成功") : RestBean.failure(500, "更新失败");
    }
}