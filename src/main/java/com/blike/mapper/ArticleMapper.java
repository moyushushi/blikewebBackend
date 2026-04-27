package com.blike.mapper;

import com.blike.entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {

    // 查询所有已发布文章，按发布时间倒序，并关联作者信息
    @Select("SELECT a.*, u.username AS author, u.avatar AS avatar " +
            "FROM article a LEFT JOIN account u ON a.user_id = u.id " +
            "WHERE a.status = 1 ORDER BY a.publish_time DESC")
    List<Article> selectAllPublished();

    // 根据ID查询文章（包含草稿，用于编辑）
    @Select("SELECT a.*, u.username AS author, u.avatar AS avatar " +
            "FROM article a LEFT JOIN account u ON a.user_id = u.id " +
            "WHERE a.id = #{id}")
    Article selectById(Integer id);

    // 插入文章（发布时 status=1，草稿 status=0）
    @Insert("INSERT INTO article(title, content, cover, category, user_id, status, publish_time) " +
            "VALUES(#{title}, #{content}, #{cover}, #{category}, #{userId}, #{status}, #{publishTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Article article);

    // 根据用户ID查询已发布的文章（status=1），按发布时间倒序
    @Select("SELECT a.*, u.username AS author, u.avatar AS avatar " +
            "FROM article a LEFT JOIN account u ON a.user_id = u.id " +
            "WHERE a.user_id = #{userId} AND a.status = 1 ORDER BY a.publish_time DESC")
    List<Article> selectByUserId(Integer userId);

    // 添加（供定时任务使用）
    @Update("UPDATE article SET view_count = view_count + #{count} WHERE id = #{id}")
    int updateViewCount(@Param("id") Integer id, @Param("count") Long count);

    @Update("UPDATE article SET like_count = like_count + #{count} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Integer id, @Param("count") Long count);

    @Update("UPDATE article SET comment_count = comment_count + #{count} WHERE id = #{id}")
    int updateCommentCount(@Param("id") Integer id, @Param("count") Long count);
}