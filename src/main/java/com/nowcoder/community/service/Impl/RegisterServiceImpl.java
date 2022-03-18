package com.nowcoder.community.service.Impl;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
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
import java.util.Random;

/**
 * 提供注册服务
 * @author wang
 * @create 2022-03-15
 */
@Service
public class RegisterServiceImpl implements CommunityConstant {

    @Autowired
    private UserServiceImpl userService;

    //用来发邮件
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    //用来拼接路径，用来当作邮件内容发送给用户，让用户点击链接激活账户
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 注册用户
     * @param user
     * @return
     */
    public Map<String,Object> register(User user){
        //由于有很多种注册失败的提示信息需要显示给前端，所以我们需要一个map来封装所有的提示信息
        Map<String,Object> map = new HashMap<>();

        //空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //规定：一个用户名或邮箱只能注册一个用户，因此我们得先查数据库是否已经有了该用户名或邮箱
        User user1 = userService.selectByName(user.getUsername());
        if(user1 != null){
            map.put("usernameMsg", "该用户名已被使用");
            return map;
        }

        User user2 = userService.selectByEmail(user.getEmail());
        if(user2 != null){
            map.put("emailMsg","邮箱已被注册过");
            return map;
        }

        //如果用户名和邮箱都没有被使用过，则可以完成注册，并添加到数据库，并完成对数据库各字段进行设置
        user.setCreateTime(new Date());
        user.setStatus(0);
        user.setType(0);
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));   //产生用来加密密码的随机字符串
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setActivationCode(CommunityUtil.generateUUID());   //产生随机的激活码
        //http://images.nowcoder.com/head/%dt.png是牛客网提供的1000个随机头像，%d代表一个数字的占位符，可以传参进去
        //String.format()方法就是解析字符串，可以动态传参进去，动态改变字符串。
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        userService.insertUser(user);

        //插入完代表注册了用户，但还需要邮箱来激活这个用户，才能正常使用

        //新建邮件内容上下文，类似往model存数据给前端显示
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //规定，要拼接处 http://localhost:8080/community/activation/101/code路径来激活
        String url = domain + contextPath + "/activation/" + user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        //需要theymeleaf模板引擎来发送html邮件
        String content = templateEngine.process("/mail/activation",context);
        //发邮件
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    /**
     * 确定是否激活码相等，从而判断是否激活成功
     * @param userId 用户的id，由于设置了id为主键，所以当插入用户之后，就会自动产生一个userId
     * @param code  用户输入的激活码，验证和数据库中的激活码是否相等
     * @return
     */
    public int activation(int userId, String code) {
        //要判断是否重复激活，需要查询该用户id的状态码是否为0，0表示没激活过
        User user = userService.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userService.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }
}
