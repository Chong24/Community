package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件驱动发系统消息
 */
public class Event {

    private String topic;    //话题，即事件类型，
    private int userId;      //触发事件的人
    private int entityType;  //实体类型，是点赞、关注还是评论
    private int entityId;
    private int entityUserId;   //实体作者，如果是点赞，那就是被点赞的人
    private Map<String,Object> data = new HashMap<>();   //为了存一些扩展的数据

    public String getTopic() {
        return topic;
    }

    //之所以set方法还设返回值，是为了以后调用方便，可以通过连续点的方式调用
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
