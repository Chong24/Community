package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对应查询数据库的Message表
 * @author wang
 * @create 2022-03-22
 */

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    List<Message> selectConversations(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    // 查询当前用户的会话数量.
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表.
    List<Message> selectLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    // 查询某个会话所包含的私信数量.
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量：这个方法包括查这个用户的总的未读私信和每个会话的未读私信，所以查询条件需要动态拼接
    int selectLetterUnreadCount( @Param("userId") int userId, @Param("conversationId") String conversationId);

    //添加私信
    int insertMessage(Message message);

    // 修改消息的状态，支持一次性读取多条消息
    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);

    // 查询某个主题下最新的通知
    Message selectLatestNotice(@Param("userId") int userId, @Param("topic") String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(@Param("userId") int userId, @Param("topic") String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(@Param("userId") int userId, @Param("topic") String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(@Param("userId") int userId, @Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);
}
