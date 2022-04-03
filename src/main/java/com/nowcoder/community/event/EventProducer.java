package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 事件的生产者；给指定的话题发消息，然后消费者就会自动从这个话题中接收消息
 * @author wang
 * @create 2022-03-28
 */
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    //处理事件：即给话题发消息
    public void fireEvent(Event event){
        //将事件发送给指定的主题，以json字符串的格式，即将事件对象转为json字符串
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
