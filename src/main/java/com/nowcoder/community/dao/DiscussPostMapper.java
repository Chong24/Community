package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 查询帖子表格的方法：需要注意的是：当方法有多个参数，一定要标@Param注解起别名，要不然mybatis识别不到#{userId}，就只能用arg0或param1
 * @author wang
 * @create 2022-03-10
 */
@Mapper
public interface DiscussPostMapper {

    /**
     * 该方法就是用来查询每页显示的数据的
     * @param userId    用户ID，之所以需要这个参数是为了以后查询该用户发了多少个帖子（可能也需要分页），
     *                  现阶段不需要（因此需要动态判断）
     * @param offset    查询数据库的起始位置
     * @param limit     查多少条
     * @return  返回查询到的数据
     */
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询总共有多少个帖子
     *  @Param注解用于给参数取别名, 如果只有一个参数,并且在<if>里使用,则必须加别名.
     * @param userId    //如果需要userId，就代表查询的是该用户发了多少个帖子
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 发布帖子
     * @param discussPost
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 根据用户id查询发布的贴子
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 更新帖子数量
     * @param id
     * @param commentCount
     * @return
     */
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    int deleteDiscussPostById(int id);
}
