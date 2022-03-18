package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LoginTicketService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wang
 * @create 2022-03-16
 */
@Service
public class LoginTicketServiceImpl implements LoginTicketService {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public int insertLoginTicket(LoginTicket loginTicket) {
        return loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Override
    public LoginTicket selectByTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    @Override
    public int updateStatus(String ticket, int status) {
        return loginTicketMapper.updateStatus(ticket,status);
    }

    /**
     * 与注册同理，因为可能出现很多错误的情形，所以我们用map封装这些错误信息，交给前端页面友好交互
     * map、model里面的数据会被放在request的请求域
     * @param username 用户名
     * @param password 密码
     * @param expiredSeconds 登陆凭证过期时间
     * @return
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String,Object> map = new HashMap<>();

        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }

        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //根据用户名查询数据库，是否该用户没注册
        User user = userService.selectByName(username);
        if (user == null){
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }
        
        //验证密码是否正确
        password = CommunityUtil.md5(password + user.getSalt());
        if (!password.equals(user.getPassword())){
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        //走到这，说明登录成功，于是生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 注销用户
     * @param ticket 即根据登陆凭证，将其状态改为1
     */
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }

    /**
     * 更新密码
     * @param email
     * @return
     */
    public Map<String, Object> codeByEmail(String email){
        Map<String,Object> map = new HashMap<>();

        User user = userService.selectByEmail(email);

        if(user == null){
            map.put("emailMsg","该邮箱没注册");
            return map;
        }

        map.put("user",user);

        Context context = new Context();
        context.setVariable("email",email);
        context.setVariable("code",user.getActivationCode());
        String url = domain + contextPath + "/forget";
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/updatePassord",context);
        //发邮件
        mailClient.sendMail(email,"更换密码",content);

        return map;
    }
}
