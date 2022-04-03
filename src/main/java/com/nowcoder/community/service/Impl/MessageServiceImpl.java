package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author wang
 * @create 2022-03-22
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> selectConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId,offset,limit);
    }

    @Override
    public int selectConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    @Override
    public List<Message> selectLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    @Override
    public int selectLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    @Override
    public int selectLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    @Override
    public int insertMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    @Override
    public int updateStatus(List<Integer> ids, int status) {
        return messageMapper.updateStatus(ids,status);
    }

    @Override
    public Message selectLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId,topic);
    }

    @Override
    public int selectNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId,topic);
    }

    @Override
    public int selectNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

    @Override
    public List<Message> selectNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId,topic,offset,limit);
    }

    public int deleteMessage(int id) {
        return messageMapper.updateStatus(Arrays.asList(new Integer[]{id}), 2);
    }
}
