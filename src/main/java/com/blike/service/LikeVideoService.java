package com.blike.service;

import com.blike.entity.LikedVideoVO;
import java.util.List;

public interface LikeVideoService {
    void likeVideo(Integer userId, Integer videoId);
    void unlikeVideo(Integer userId, Integer videoId);
    List<LikedVideoVO> getLikedVideos(Integer userId, int limit);

    boolean isLiked(Integer userId, Integer videoId);
}