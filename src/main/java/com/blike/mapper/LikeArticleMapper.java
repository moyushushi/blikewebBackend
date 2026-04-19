package com.blike.mapper;

import com.blike.entity.Article;
import com.blike.entity.LikedArticleVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LikeArticleMapper {

    @Insert("INSERT INTO user_like_article(user_id, article_id) VALUES(#{userId}, #{articleId})")
    int insert(LikedArticleVO record);

    @Delete("DELETE FROM user_like_article WHERE user_id = #{userId} AND article_id = #{articleId}")
    int delete(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    @Select("SELECT COUNT(*) > 0 FROM user_like_article WHERE user_id = #{userId} AND article_id = #{articleId}")
    boolean exists(@Param("userId") Integer userId, @Param("articleId") Integer articleId);

    @Select("SELECT article_id FROM user_like_article WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<Integer> findLikedArticleIds(@Param("userId") Integer userId, @Param("limit") int limit);

    // 获取用户点赞的文章详情（连表查询）
    @Select("SELECT a.*, u.username AS author, u.avatar AS avatar, l.create_time AS likeTime " +
            "FROM user_like_article l " +
            "LEFT JOIN article a ON l.article_id = a.id " +
            "LEFT JOIN account u ON a.user_id = u.id " +
            "WHERE l.user_id = #{userId} ORDER BY l.create_time DESC LIMIT #{limit}")
    List<Article> findLikedArticlesWithDetail(@Param("userId") Integer userId, @Param("limit") int limit);
}