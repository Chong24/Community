package com.nowcoder.community.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * 帖子的评论
 */
@Data
@ToString
public class Comment {

    private int id;
    private int userId;         //用户id
    private int entityType;     //实体类型，区分是给什么类型评论，例如视频、帖子、题等等，都有评论功能
    private int entityId;       //帖子id，区分给第几个帖子评论
    private int targetId;       //区分目标，是在同一个评论下回复，还是自己评论
    private String content;     //帖子内容
    private int status;         //帖子状态 1-被删除
    private Date createTime;    //评论时间
}
