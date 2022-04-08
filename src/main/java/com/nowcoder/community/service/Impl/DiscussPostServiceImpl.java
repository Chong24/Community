package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 对帖子数据查询服务
 * @author wang
 * @create 2022-03-10
 */
@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    //orderMode用于切换首页显示的是正常排序还是最热排序
    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        return discussPostMapper.selectDiscussPosts(userId,offset,limit, orderMode);
    }

    @Override
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Override
    public int insertDiscussPost(DiscussPost discussPost) {
        //插入前，我们需要过滤一些敏感词汇
        if (discussPost == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //当用户输入一些html标签时，为了不让服务器误以为是标签，需要对其进行处理，以特殊转义符存在服务器中
        //为什么要用 HtmlUtils.htmlEscape？ 因为有些同学在恶意注册的时候，
        // 会使用诸如 <script>alert('papapa')</script> 这样的名称，会导致网页打开就弹出一个对话框。 那么在转义之后，就没有这个问题了。
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    @Override
    public DiscussPost selectDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }


    public int updateCommentCount(int entityId, int count) {
        return discussPostMapper.updateCommentCount(entityId,count);
    }

    @Override
    public int deleteDiscussPostById(int id) {
        return discussPostMapper.deleteDiscussPostById(id);
    }

    @Override
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id,type);
    }

    @Override
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id,status);
    }

    @Override
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id,score);
    }
}
