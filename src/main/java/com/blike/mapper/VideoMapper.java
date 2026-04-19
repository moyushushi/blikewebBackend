package com.blike.mapper;

import com.blike.entity.Video;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VideoMapper {

    // 查询所有视频，并关联用户信息
    @Select("SELECT v.*, u.username AS author, u.avatar AS avatar " +
            "FROM video v " +
            "LEFT JOIN account u ON v.user_id = u.id")
    List<Video> selectAll();

    // 根据 ID 查询视频详情，关联用户信息
    @Select("SELECT v.*, u.username AS author, u.avatar AS avatar " +
            "FROM video v " +
            "LEFT JOIN account u ON v.user_id = u.id " +
            "WHERE v.id = #{id}")
    Video selectById(Integer id);

    // 插入视频时不再需要 author 和 avatar 字段
    @Insert("INSERT INTO video(title, cover, url, playCount, time, `desc`, publishTime, likeCount, commentCount, category, user_id) " +
            "VALUES(#{title}, #{cover}, #{url}, #{playCount}, #{time}, #{desc}, #{publishTime}, #{likeCount}, #{commentCount}, #{category}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Video video);

    @Update("UPDATE video SET commentCount = commentCount + 1 WHERE id = #{videoId}")
    int incrementCommentCount(Integer videoId);

    // 增加点赞数
    @Update("UPDATE video SET likeCount = likeCount + 1 WHERE id = #{videoId}")
    int incrementLikeCount(Integer videoId);

    // 减少点赞数
    @Update("UPDATE video SET likeCount = likeCount - 1 WHERE id = #{videoId}")
    int decrementLikeCount(Integer videoId);

    @Select("SELECT v.*, u.username AS author, u.avatar AS avatar " +
            "FROM video v " +
            "LEFT JOIN account u ON v.user_id = u.id " +
            "WHERE v.user_id = #{userId} " +
            "ORDER BY v.publishTime DESC")
    List<Video> selectByUserId(Integer userId);

    @Update("UPDATE video SET playCount = playCount + 1 WHERE id = #{id}")
    int incrementPlayCount(Integer id);

}

