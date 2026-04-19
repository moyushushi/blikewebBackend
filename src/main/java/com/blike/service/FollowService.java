package com.blike.service;

import com.blike.entity.FollowUserVO;
import java.util.List;

public interface FollowService {
    void follow(Integer followerId, Integer followingId);
    void unfollow(Integer followerId, Integer followingId);
    boolean isFollowing(Integer followerId, Integer followingId);
    List<FollowUserVO> getFollowingList(Integer userId);
}