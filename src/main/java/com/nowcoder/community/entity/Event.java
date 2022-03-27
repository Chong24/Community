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
    private Map<String,Object> data = new HashMap<>();   //为了存一些扩展数据
}
