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

    // 增加阅读数
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Integer id);

    // 更新点赞数（delta 为 +1 或 -1）
    @Update("UPDATE article SET like_count = like_count + #{delta} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Integer id, @Param("delta") int delta);

    // 更新评论数
    @Update("UPDATE article SET comment_count = comment_count + #{delta} WHERE id = #{id}")
    int updateCommentCount(@Param("id") Integer id, @Param("delta") int delta);

    // 根据用户ID查询已发布的文章（status=1），按发布时间倒序
    @Select("SELECT a.*, u.username AS author, u.avatar AS avatar " +
            "FROM article a LEFT JOIN account u ON a.user_id = u.id " +
            "WHERE a.user_id = #{userId} AND a.status = 1 ORDER BY a.publish_time DESC")
    List<Article> selectByUserId(Integer userId);
}