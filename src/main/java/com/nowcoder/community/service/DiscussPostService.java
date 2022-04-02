package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;

import java.util.List;

/**
 * @author wang
 * @create 2022-03-10
 */
public interface DiscussPostService {


    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit);

    public int findDiscussPostRows(int userId);

    public int insertDiscussPost(DiscussPost discussPost);

    public DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int deleteDiscussPostById(int id);
}
