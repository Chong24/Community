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

}