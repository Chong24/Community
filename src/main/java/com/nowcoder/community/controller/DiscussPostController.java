package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.Impl.DiscussPostServiceImpl;
import com.nowcoder.community.service.Impl.UserServiceImpl;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.smartcardio.CommandAPDU;
import java.util.Date;

/**
 * 处理帖子的控制器
 */

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private UserServiceImpl userService;

    /**
     * 将用户发布的贴子存到数据库中，返回JSON类型的交互信息
     * @param title
     * @param content
     * @return
     */
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

        //可能会出现报错的情况，以后统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    /**
     * 查询帖子
     * @return
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model){
        //查询帖子
        DiscussPost post = discussPostService.selectDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //根据帖子查询用户，显示用户信息的时候用，例如头像
        User user = userService.selectById(post.getUserId());
        model.addAttribute("user",user);

        return "/site/discuss-detail";
    }
}
