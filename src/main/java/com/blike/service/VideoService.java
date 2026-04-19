package com.blike.service;

import com.blike.entity.FollowUserVO;
import com.blike.entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    void unfollow(Integer followerId, Integer followingId);

    boolean isFollowing(Integer followerId, Integer followingId);

    List<FollowUserVO> getFollowingList(Integer userId);

    // 视频上传结果（包含URL和时长）
    class VideoUploadResult {
        private String url;
        private String duration;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }

    void follow(Integer followerId, Integer followingId);

    List<Video> getVideoList();

    Video getVideoById(Integer id);

    String uploadCover(MultipartFile file);

    // 修改返回类型为 VideoUploadResult
    VideoUploadResult uploadVideo(MultipartFile file);

    String uploadAvatar(MultipartFile file);

    Video publishVideo(MultipartFile videoFile, MultipartFile coverFile, String title, String desc, String category, Integer userId);

    List<Video> getVideosByUserId(Integer userId);

    void incrementPlayCount(Integer videoId);

}