package com.nowcoder.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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
@Document(indexName = "discusspost", shards = 6, replicas = 3)
public class DiscussPost {

    @Id
    private int id;

    @Field(type = FieldType.Integer)
    private int userId;                 //发布该帖子的用户Id

    // 交给哪些分词器分词搜索
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;               //帖子的标题

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;             //帖子的内容

    @Field(type = FieldType.Integer)
    private int type;                   //判断是否是置顶

    @Field(type = FieldType.Integer)
    private int status;                 //判断是否是精华 2表示拉黑

    @Field(type = FieldType.Date)
    private Date createTime;            //帖子的发布时间

    @Field(type = FieldType.Integer)
    private int commentCount;           //帖子的评论数量

    @Field(type = FieldType.Double)
    private double score;               //帖子的分数
}
