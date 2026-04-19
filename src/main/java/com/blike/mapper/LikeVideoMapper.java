package com.blike.mapper;

import com.blike.entity.LikedVideoVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LikeVideoMapper {

    // 点赞
    @Insert("INSERT INTO user_like_video(user_id, video_id) VALUES(#{userId}, #{videoId})")
    int insert(@Param("userId") Integer userId, @Param("videoId") Integer videoId);

    // 取消点赞
    @Delete("DELETE FROM user_like_video WHERE user_id = #{userId} AND video_id = #{videoId}")
    int delete(@Param("userId") Integer userId, @Param("videoId") Integer videoId);

    // 查询用户点赞的视频列表（带视频详情和点赞时间）
    @Select("SELECT v.id, v.title, v.cover, u.username AS author, v.playCount, v.time, l.create_time AS likeTime " +
            "FROM user_like_video l " +
            "LEFT JOIN video v ON l.video_id = v.id " +
            "LEFT JOIN account u ON v.user_id = u.id " +   // 关联 account 表获取作者名
            "WHERE l.user_id = #{userId} " +
            "ORDER BY l.create_time DESC " +
            "LIMIT #{limit}")
    List<LikedVideoVO> selectLikedVideosByUserId(@Param("userId") Integer userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) > 0 FROM user_like_video WHERE user_id = #{userId} AND video_id = #{videoId}")
    boolean exists(@Param("userId") Integer userId, @Param("videoId") Integer videoId);
}