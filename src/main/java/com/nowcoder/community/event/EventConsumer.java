package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.Impl.DiscussPostServiceImpl;
import com.nowcoder.community.service.Impl.ElasticsearchServiceImpl;
import com.nowcoder.community.service.Impl.MessageServiceImpl;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件的消费者：从话题中接收数据
 * @author wang
 * @create 2022-03-28
 */
@Component
public class EventConsumer implements CommunityConstant {

    //打印日志信息
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageServiceImpl messageService;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private ElasticsearchServiceImpl elasticsearchService;

    //@KafkaListener用来监听生产者发布的消息，一个方法接收三种话题的消息
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息为空");
            return;
        }

        //接收生产者的消息，因为发送的是JSON字符串形式，我们要还原为Event对象形式
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null){
            logger.error("消息格式错误");
            return;
        }

        //走到这，说明拿到了正确格式的消息，就得把这消息存在数据库中
        //系统给我们发消息是以私信的方式发的，所以要用Message对象
        Message message = new Message();
        //系统发的，默认id为1
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());   //以点赞为例，就是被点赞的那个人
        message.setCreateTime(new Date());
        //由于ConversationId是由fromId和toId拼接而成的，fromId恒为1，这个数据就没意义了，所以把它改存为话题名称
        message.setConversationId(event.getTopic());

        //事件的其他数据存在一个map中，作为系统给用户发送的消息内容，用于客户点显示
        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        //事件中如果还有其他扩展数据，也存在content中；比如评论和回复的时候data中存帖子id。
        if (!event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.insertMessage(message);
    }

    //接收发帖子的topic
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);

        if (event == null){
            logger.error("消息格式错误");
            return;
        }

        DiscussPost post = discussPostService.selectDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    //接收删除帖子的topic
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);

        if (event == null){
            logger.error("消息格式错误");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }
}
