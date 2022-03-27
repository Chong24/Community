package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

/**
 * springboot整合Kafka测试
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka(){
        kafkaProducer.sendMessage("test","kobe");
        kafkaProducer.sendMessage("test","wcxx");

        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 消费者：放在IOC容器中
 */
@Component
class KafkaConsumer{
    //监听topic为test的消息，即订阅，然后ConsumeRecord存了消息
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}

/**
 * 生产者
 */
@Component
class KafkaProducer{

    //引入包之后，有KafkaTemplate可以操作
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content){
        kafkaTemplate.send(topic,content);
    }
}
