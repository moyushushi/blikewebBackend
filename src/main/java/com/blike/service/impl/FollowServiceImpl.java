package com.blike.service.impl;

import com.blike.entity.Follow;
import com.blike.entity.FollowUserVO;
import com.blike.mapper.FollowMapper;
import com.blike.service.FollowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FollowServiceImpl implements FollowService {

    @Resource
    private FollowMapper followMapper;

    @Override
    public void follow(Integer followerId, Integer followingId) {
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        followMapper.insert(follow);
    }

    @Override
    public void unfollow(Integer followerId, Integer followingId) {
        followMapper.delete(followerId, followingId);
    }

    @Override
    public boolean isFollowing(Integer followerId, Integer followingId) {
        return followMapper.exists(followerId, followingId);
    }

    @Override
    public List<FollowUserVO> getFollowingList(Integer userId) {
        return followMapper.selectFollowingByUserId(userId);
    }
}