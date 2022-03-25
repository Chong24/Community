package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 查询评论的表
 * @author wang
 * @create 2022-03-21
 */
@Mapper
public interface CommentMapper {

    /**
     * 查询评论
     * @param entityType 类型，即是查询视频的评论还是讨论区的评论
     * @param entityId  帖子id，区分给第几个帖子评论
     * @param offset    起始
     * @param limit     每页显示
     * @return
     */
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询该帖子的评论总数
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    /**
     * 添加评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);

    /**
     * 根据id查评论
     * @param id
     * @return
     */
    Comment selectCommentById(int id);

    /**
     * 查询该用户评论过的帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> selectCommentsByUser(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 该用户评论的帖子的数量
     * @param userId
     * @return
     */
    int selectCountByUser(int userId);
}
