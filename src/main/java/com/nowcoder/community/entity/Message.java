package com.nowcoder.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 私信
 * @author wang
 * @create 2022-03-22
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private int id;                 //自增主键id
    private int fromId;             //这个私信的发送者  1-代表是系统发送的
    private int toId;               //这个私信的接收者
    private String conversationId;  //由formId、toId拼接而成，代表是这两个用户之间的对话。用户查询会话详情
    private String content;         //私信的内容
    private int status;             //私信的状态 0-未读 1-正常 2-删除
    private Date createTime;        //私信的创建时间

}
