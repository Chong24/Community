package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.Impl.MessageServiceImpl;
import com.nowcoder.community.service.Impl.UserServiceImpl;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * 处理私信的
 * @author wang
 * @create 2022-03-22
 */
@Controller
public class MessageController implements CommunityConstant{

    @Autowired
    private MessageServiceImpl messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserServiceImpl userService;

    //私信列表
    @LoginRequired
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {
        //首先要获得用户，知道查看的是谁的私信
        User user = hostHolder.getUser();

        //分页信息，起始就是设置limit、总数row、路径path即可
        page.setPath("/letter/list");
        page.setLimit(5);
        page.setRows(messageService.selectConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.selectConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if (conversationList != null){
            for (Message message : conversationList) {
                Map<String,Object> map = new HashMap<>();
                //获取该私信对话框中最新的一条私信显示
                map.put("conversation",message);
                //获取私信总数
                map.put("letterCount",messageService.selectLetterCount(message.getConversationId()));
                //获取每个私信中未读的消息总数
                map.put("unreadCount",messageService.selectLetterUnreadCount(user.getId(),message.getConversationId()));

                //获取与用户交流的用户id
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.selectById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //查询未读消息总数量
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";
    }

    /**
     * 显示私信详情
     * @param conversationId
     * @param page
     * @param model
     * @return
     */
    @LoginRequired
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model){
        //分页信息
        page.setRows(messageService.selectLetterCount(conversationId));
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);

        //私信列表
        List<Message> letterList = messageService.selectLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if (letterList != null){
            for (Message message : letterList) {
                Map<String,Object> map = new HashMap<>();
                //获取私信的内容
                map.put("letter",message);
                //获取这个私信的发送者
                map.put("fromUser",userService.selectById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);

        //获取与当前用户私信的人
        model.addAttribute("target",getLetterTarget(conversationId));

        //既然点开了详情查看私信，那么意味着这私信可以标记为已读了,ids就为用户收到的私信id
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.updateStatus(ids,1);
        }

        return "/site/letter-detail";
    }

    /**
     * 获取发送给当前登录用户的所有未读私信id
     * @param letterList
     * @return
     */
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();

        if (letterList != null){
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 根据conversationId来判断与当前用户私信的人
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.selectById(id1);
        } else {
            return userService.selectById(id0);
        }
    }

    @LoginRequired
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String content){
        //获取你要发送的用户
        User target = userService.selectByName(toName);
        if (target == null){
            //用JSON传到前端去交互
            return CommunityUtil.getJSONString(1,"用户不存在");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        //拼接coonversation_id
        if (message.getToId() > message.getFromId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else{
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.insertMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    // 删除私信
    @LoginRequired
    @RequestMapping(path = "/letter/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(int id) {
        messageService.deleteMessage(id);
        return CommunityUtil.getJSONString(0);
    }

    @LoginRequired
    @GetMapping("/notice/list")
    public String getNoticeList(Model model){
        //由于是查询登录用户的通知，所以首先得获取登录用户
        User user = hostHolder.getUser();

        //查询评论类的通知
        Message message = messageService.selectLatestNotice(user.getId(), TOPIC_COMMENT);
        if (message != null){
            Map<String,Object> messageVo = new HashMap<>();
            messageVo.put("message",message);

            //获取数据库中存的通知内容，由于数据库存的时候有很多转义字符，需要对其进行处理
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.selectById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.selectNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("count",count);

            int unread = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("unread",unread);

            model.addAttribute("commentNotice",messageVo);
        }

        // 查询点赞类通知
        message = messageService.selectLatestNotice(user.getId(), TOPIC_LIKE);
        if (message != null){
            Map<String,Object> messageVo = new HashMap<>();
            messageVo.put("message",message);

            //获取数据库中存的通知内容，由于数据库存的时候有很多转义字符，需要对其进行处理
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.selectById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.selectNoticeCount(user.getId(), TOPIC_LIKE);
            messageVo.put("count",count);

            int unread = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVo.put("unread",unread);

            model.addAttribute("likeNotice",messageVo);
        }

        message = messageService.selectLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null){
            Map<String,Object> messageVo = new HashMap<>();
            messageVo.put("message",message);

            //获取数据库中存的通知内容，由于数据库存的时候有很多转义字符，需要对其进行处理
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.selectById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.selectNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("count",count);

            int unread = messageService.selectNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("unread",unread);

            model.addAttribute("followNotice",messageVo);
        }

        //查询未读消息数量
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }
    
    @LoginRequired
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model){
        User user = hostHolder.getUser();
        
        page.setRows(messageService.selectNoticeCount(user.getId(),topic));
        page.setLimit(5);
        page.setPath("/notice/detail/"+topic);

        List<Message> noticeList = messageService.selectNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        if (noticeList != null){
            List<Map<String,Object>> noticeVoList = new ArrayList<>();
            for (Message notice : noticeList) {
                Map<String,Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
                map.put("user",userService.selectById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                //通知作者
                map.put("fromUser",userService.selectById(notice.getFromId()));
                noticeVoList.add(map);
            }
            model.addAttribute("notices", noticeVoList);
        }

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.updateStatus(ids,1);
        }

        return "/site/notice-detail";
    }
}
