package com.blike.mapper;

import com.blike.entity.Follow;
import com.blike.entity.FollowUserVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowMapper {

    @Insert("INSERT INTO user_follow(follower_id, following_id) VALUES(#{followerId}, #{followingId})")
    int insert(Follow follow);

    @Delete("DELETE FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    int delete(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);

    @Select("SELECT COUNT(*) > 0 FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    boolean exists(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);

    @Select("SELECT u.id, u.username, u.avatar, f.create_time AS followTime " +
            "FROM user_follow f " +
            "LEFT JOIN account u ON f.following_id = u.id " +
            "WHERE f.follower_id = #{userId} " +
            "ORDER BY f.create_time DESC")
    List<FollowUserVO> selectFollowingByUserId(Integer userId);
}