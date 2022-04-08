package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.Impl.CommentServiceImpl;
import com.nowcoder.community.service.Impl.DiscussPostServiceImpl;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * 处理评论的控制器
 * @author wang
 * @create 2022-03-21
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentServiceImpl commentService;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private HostHolder hostHolder;

    //生产者
    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加评论，添加完应该转到那个帖子下面
     * @param discussPostId
     * @param comment
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.insertComment(comment);

        //触发评论事件，系统给用户发消息，谁评论了你；data中带帖子id，是为了让系统给用户发送的消息中可以跳转查看帖子
        Event event = new Event().setTopic(TOPIC_COMMENT).setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType()).setEntityId(comment.getEntityId()).setData("postId",discussPostId);
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //根据帖子的类型id，查询是哪个用户发的
            DiscussPost target = discussPostService.selectDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
            //根据评论的类型id，查询是哪个用户发的
            Comment target = commentService.selectCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        //触发事件，即生产者开始工作
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPostId);

        return "redirect:/discuss/detail/" + discussPostId;
    }

}
