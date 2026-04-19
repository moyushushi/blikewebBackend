package com.blike.mapper;

import com.blike.entity.Comment;
import com.blike.entity.CommentVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {
    @Insert("INSERT INTO comment(video_id, user_id, content, create_time) VALUES(#{videoId}, #{userId}, #{content}, #{createTime})")
    int insert(Comment comment);
    @Select("SELECT * FROM comment WHERE video_id = #{videoId} ORDER BY create_time DESC")
    List<Comment> selectByVideoId(Integer videoId);
    // 新增：根据视频ID查询评论列表，关联用户表获取用户名和头像
    @Select("SELECT c.id, c.video_id AS videoId, c.user_id AS userId, c.content, c.create_time AS createTime, " +
            "u.username, u.avatar " +
            "FROM comment c " +
            "LEFT JOIN account u ON c.user_id = u.id " +
            "WHERE c.video_id = #{videoId} " +
            "ORDER BY c.create_time DESC")
    List<CommentVO> selectByVideoIdWithUser(Integer videoId);
}