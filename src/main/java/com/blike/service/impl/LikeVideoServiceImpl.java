package com.blike.service.impl;

import com.blike.mapper.LikeVideoMapper;
import com.blike.mapper.VideoMapper;
import com.blike.service.LikeVideoService;
import com.blike.utils.RedisCountUtil;
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
    private RedisCountUtil redisCountUtil;

    @Override
    @Transactional
    public void likeVideo(Integer userId, Integer videoId) {
        likeVideoMapper.insert(userId, videoId);
        redisCountUtil.incrementLikeCount(videoId, 1);
    }

    @Override
    @Transactional
    public void unlikeVideo(Integer userId, Integer videoId) {
        likeVideoMapper.delete(userId, videoId);
        redisCountUtil.incrementLikeCount(videoId, -1);
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