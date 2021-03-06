package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.Impl.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 处理帖子的控制器
 */

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant{

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private CommentServiceImpl commentService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private LikeServiceImpl likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private ElasticsearchServiceImpl elasticsearchService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 将用户发布的贴子存到数据库中，返回JSON类型的交互信息
     * @param title
     * @param content
     * @return
     */
    @LoginRequired
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403,"你还没有登陆");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.insertDiscussPost(post);

        // 触发发帖事件，以便消费者收到消息后可以存在Elasticsearch的数据库中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,post.getId());

        //可能会出现报错的情况，以后统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    /**
     * 查询帖子，包括评论的数据，分页显示
     * 请求传入的page属性相关的参数，都会自动封装到JavaBean中
     * @return
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //查询帖子
        DiscussPost post = discussPostService.selectDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //根据帖子查询用户，显示用户信息的时候用，例如头像
        User user = userService.selectById(post.getUserId());
        model.addAttribute("user",user);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态，如果用户没登陆只能看到赞，看不到数量和赞的状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //评论的分页信息，Page类型的形参会自动装在model中
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.selectCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        // 评论VO列表：即一个帖子下的评论信息
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentVoList != null){
            //遍历帖子下的评论，获取评论和回复和用户信息，封装到map中
            for (Comment comment : commentList) {
                //评论VO
                Map<String,Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //作者，因为需要知道是谁在评论
                commentVo.put("user",userService.selectById(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态，如果用户没登陆只能看到赞，看不到数量和赞的状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                //获取一个评论下的回复列表，回复就不给它分页了
                List<Comment> replyList = commentService.selectCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(),0,Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyVoList != null){
                    //遍历回复
                    for (Comment reply : replyList) {
                       Map<String,Object> replyVo = new HashMap<>();
                       //回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",userService.selectById(reply.getUserId()));
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态，如果用户没登陆只能看到赞，看不到数量和赞的状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.selectById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                //回复数量
                int replyCount = commentService.selectCountByEntity(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }

    @LoginRequired
    @ResponseBody
    @GetMapping("/delete/{discussPostId}")
    public String addDiscussPost(@PathVariable("discussPostId") int discussPostId) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登陆");
        }
        discussPostService.deleteDiscussPostById(discussPostId);
        elasticsearchService.deleteDiscussPost(discussPostId);

        return CommunityUtil.getJSONString(0,"删除成功");
    }

    //处理置顶请求：异步请求的方式
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id){
        //type = 1代表置顶
        discussPostService.updateType(id,1);

        //触发发帖事件，因为要更新ElasticSearch库中帖子
        Event event = new Event().setTopic(TOPIC_PUBLISH).setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST).setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/unTop")
    @ResponseBody
    public String setUnTop(int id){
        //type = 1代表置顶
        discussPostService.updateType(id,0);

        //触发发帖事件，因为要更新ElasticSearch库中帖子
        Event event = new Event().setTopic(TOPIC_PUBLISH).setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST).setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //加精
    //处理置顶请求：异步请求的方式
    @PostMapping("/wonderful")
    @ResponseBody
    public String setUnWonderful(int id){
        //status = 1代表加精，2代表删除
        discussPostService.updateStatus(id,1);

        //触发发帖事件，因为要更新ElasticSearch库中帖子
        Event event = new Event().setTopic(TOPIC_PUBLISH).setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST).setEntityId(id);
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/unWonderful")
    @ResponseBody
    public String setWonderful(int id){
        //status = 1代表加精，2代表删除
        discussPostService.updateStatus(id,0);

        //触发发帖事件，因为要更新ElasticSearch库中帖子
        Event event = new Event().setTopic(TOPIC_PUBLISH).setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST).setEntityId(id);
        eventProducer.fireEvent(event);

        //取消计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().remove(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }
}
