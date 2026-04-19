package com.blike.service.impl;

import com.blike.mapper.LikeVideoMapper;
import com.blike.mapper.VideoMapper;
import com.blike.service.LikeVideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.blike.entity.LikedVideoVO;

@Service
public class LikeVideoServiceImpl implements LikeVideoService {

    @Resource
    private LikeVideoMapper likeVideoMapper;

    @Resource
    private VideoMapper videoMapper;

    @Override
    @Transactional
    public void likeVideo(Integer userId, Integer videoId) {
        likeVideoMapper.insert(userId, videoId);
        videoMapper.incrementLikeCount(videoId);
    }

    @Override
    @Transactional
    public void unlikeVideo(Integer userId, Integer videoId) {
        likeVideoMapper.delete(userId, videoId);
        videoMapper.decrementLikeCount(videoId);
    }

    @Override
    public List<LikedVideoVO> getLikedVideos(Integer userId, int limit) {
        return likeVideoMapper.selectLikedVideosByUserId(userId, limit);
    }
    @Override
    public boolean isLiked(Integer userId, Integer videoId) {
        return likeVideoMapper.exists(userId, videoId);
    }
}