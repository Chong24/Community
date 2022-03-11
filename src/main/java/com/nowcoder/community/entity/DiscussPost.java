package com.nowcoder.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 帖子的JavaBean
 * @author wang
 * @create 2022-03-10
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DiscussPost {
    private int id;
    private int userId;         //发布该帖子的用户Id
    private String title;       //帖子的标题
    private String content;     //帖子的内容
    private int type;           //判断是否是置顶
    private int status;         //判断是否是精华 2表示拉黑
    private Date createTime;    //帖子的发布时间
    private int commentCount;   //帖子的评论数量
    private double score;       //帖子的分数
}
