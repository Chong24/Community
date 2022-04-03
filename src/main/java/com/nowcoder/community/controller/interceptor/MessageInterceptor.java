package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 消息拦截器：为了显示首页的消息数，因为这个是任意一个页面都需要显示的
 * 换句话说就是任何请求处理模板之前都需要拿到这个数据，所以可以用拦截器来实现，
 * 拦截在执行完控制器方法之后，执行模板之前
 * @author wang
 * @create 2022-03-28
 */
@Component
public class MessageInterceptor implements HandlerInterceptor{

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //消息总数就是私信未读数+系统通知未读数
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
            int noticeUnreadCount = messageService.selectNoticeUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount",letterUnreadCount+noticeUnreadCount);
        }
    }
}
