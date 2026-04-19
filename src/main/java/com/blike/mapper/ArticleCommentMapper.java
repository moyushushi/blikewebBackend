package com.blike.mapper;

import com.blike.entity.ArticleComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleCommentMapper {

    // 查询某文章的所有评论，按时间正序，并关联用户信息
    @Select("SELECT c.*, u.username, u.avatar AS userAvatar " +
            "FROM article_comment c LEFT JOIN account u ON c.user_id = u.id " +
            "WHERE c.article_id = #{articleId} ORDER BY c.create_time ASC")
    List<ArticleComment> selectByArticleId(Integer articleId);

    // 插入评论
    @Insert("INSERT INTO article_comment(article_id, user_id, content) VALUES(#{articleId}, #{userId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ArticleComment comment);

    // 删除评论（可选）
    @Delete("DELETE FROM article_comment WHERE id = #{id}")
    int deleteById(Integer id);

    @Update("UPDATE article SET comment_count = comment_count + #{delta} WHERE id = #{articleId}")
    void updateCommentCount(@Param("articleId") Integer articleId, @Param("delta") int delta);
}