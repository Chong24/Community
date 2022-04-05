package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.Impl.LoginTicketServiceImpl;
import com.nowcoder.community.service.Impl.UserServiceImpl;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 登录的拦截器：没有登录则不能访问一些页面，且不能通过地址跳过登录页面访问其他资源
 * 自定义的拦截器：关键是要实现HandlerInterceptor，然后在自定义的配置类中添加拦截器
 * @author wang
 * @create 2022-03-17
 */

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(HandlerInterceptor.class);

    @Autowired
    private LoginTicketServiceImpl loginTicketService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 执行控制器之前会执行，起到拦截作用，return true表示放行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("preHandle: " + handler.toString());
        //首先要从请求中获取cookie，拿到登录凭证，查看是哪个用户在操作
        String ticket = CookieUtil.getValue(request, "ticket");
        System.out.println(ticket);

        if (ticket != null){
            //由登录凭证获取用户状态，拿到用户id
            LoginTicket loginTicket = loginTicketService.selectByTicket(ticket);
            //判断凭证是否有效：即是否过期、是否已登录(这就是注销为啥将其状态置为0)、是否有该凭证
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查到用户
                User user = userService.selectById(loginTicket.getUserId());
                //存在有线程保护的threadlocal中
                hostHolder.setUser(user);
                //由于我们用的是自己实现的自定义规则，所以我们要在用户登陆之后，构建用户认证的结果
                //存入SecurityContext,以便于Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    /**
     * 执行完控制器方法后执行，在执行模板引擎之前，所以需要在这个阶段将User的数据存在modelAndView给前端显示
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.debug("postHandle: " + handler.toString());
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }
    }

    /**
     * 执行完modelAndView模板引擎渲染后执行
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("afterCompletion: " + handler.toString());
        //当所有方法完成后，清除ThreadLocal中存的数据
        hostHolder.clear();
    }
}
