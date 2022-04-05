package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.config.KaptchaConfig;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.Impl.LoginTicketServiceImpl;
import com.nowcoder.community.service.Impl.RegisterServiceImpl;
import com.nowcoder.community.service.Impl.UserServiceImpl;
import com.nowcoder.community.service.LoginTicketService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 登录注册的控制器
 *
 * @author wang
 * @create 2022-03-15
 */

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginTicketServiceImpl loginTicketService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RegisterServiceImpl registerService;

    //导入Producer接口实现类，即是我们重写的
    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = registerService.register(user);

        //说明没有任何的错误信息
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经发送了一封激活邮件，请尽快激活");
            model.addAttribute("target", "/index");
            //跳到激活成功的页面
            return "/site/operate-result";
        } else {
            //这里并没有具体区分是哪种错误
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            //重新返回到注册页
            return "/site/register";
        }
    }

    //由于我们邮件中还有一个激活的链接，所以我们还需要控制器去处理这个链接的请求
    // http://localhost:8080/community/activation/101/code
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = registerService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            //与用户交互信息
            model.addAttribute("msg", "激活成功，您的帐号已经可以开始使用了");
            //激活后跳转的页面
            model.addAttribute("target", "/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，该账号已激活");
            model.addAttribute("target", "/login");
        }else{
            model.addAttribute("msg", "激活失败，激活码不正确");
            model.addAttribute("target", "/login");
        }
        return "/site/operate-result";
    }

    /**
     *  由于返回的是特殊格式图片，所以需要自己返回
     * @param response 设置返回的数据类型
//     * @param session  用于存取数据给页面显示，以前用session实现的，现在用redis替代
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){

        //生成给验证码的内容
        String text = kaptchaProducer.createText();

        //生成验证码的图片
        BufferedImage image = kaptchaProducer.createImage(text);

//        //将验证码存入session
//        session.setAttribute("kaptcha",text);

        //用redis+cookie实现验证码功能
        String kaptchaOwner = CommunityUtil.generateUUID();
        //创建cookie，用cookie携带信息，并设置必要的一些参数
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);    //设置共享此cookie的路径
        response.addCookie(cookie);

        //将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        //60秒这个键将在redis过期
        redisTemplate.opsForValue().set(redisKey,text, 60, TimeUnit.SECONDS);

        //将图片输出给浏览器
        try {
            //获取输出流
            ServletOutputStream os = response.getOutputStream();
            //用这个流输出一个png格式的图片给浏览器
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败："+e.getMessage());
        }
    }

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param rememberme 是否记住我
     * @param model     存数据给前端交互
//     * @param session   获取验证码
     * @param response  将cookie从服务器给浏览器
     * @return
     */
    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/*HttpSession session, */, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {

        //首先检查验证码是否输入正确
//        String kaptcha = (String) session.getAttribute("kaptcha");

        //用cookie获得key，然后查redis获取验证码
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }


        if (StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        //检查账号、密码是否正确；需要先设置过期时间，即是否记住我，记住就过期时间长一点，这个过期时间指的是cookie的寿命
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = loginTicketService.login(username, password, expiredSeconds);
        //如果map中存了ticket，就说明登录成功了，这个时候就得创建cookie，将cookie发送给浏览器保存
        if (map.containsKey("ticket")){
            //创建cookie，一般都要设置路径和生命周期
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            //说明登录失败，就将map的错误信息给用户交互
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * 注销用户
     * @param ticket
     * @return
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        loginTicketService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    /**
     * 忘记密码，需要填写邮箱，并发邮件激活
     * @return
     */
    @GetMapping("/forget")
    public String getForgetPage(){
        return "/site/forget";
    }

    @GetMapping("/sendEmail")
    public String sendEmail(String email,Model model,HttpSession session){
        if(StringUtils.isBlank(email)){
            model.addAttribute("emailMsg","邮箱不能为空");
            return "/site/forget";
        }
        Map<String, Object> map = loginTicketService.codeByEmail(email);
        if(!map.containsKey("user")){
            model.addAttribute("emailMsg",map.get("emailMsg"));
        }
        session.setAttribute("user",map.get("user"));
        return "/site/forget";
    }

    @PostMapping("/forget")
    public String forget(Model model, String email, String code, String password, String newPassword, HttpSession session){
        if(StringUtils.isBlank(email)){
            model.addAttribute("emailMsg","邮箱不能为空");
            return "/site/forget";
        }
        if (StringUtils.isBlank(code)){
            model.addAttribute("codeMsg","验证码不能为空");
            return "/site/forget";
        }
        if(StringUtils.isBlank(password)){
            model.addAttribute("passwordMsg","密码不能为空");
            return "/site/forget";
        }

        User user = (User) session.getAttribute("user");

        if(user == null){
            model.addAttribute("codeMsg","请先获取验证码");
            return "/site/forget";
        }

        if (user.getActivationCode().equals(code)){
            if(password.equals(newPassword)){
                password = CommunityUtil.md5(password + user.getSalt());
                userService.updatePassword(user.getId(),password);
                return "/site/login";
            }else{
                model.addAttribute("newPasswordMsg","密码不一致");
            }
        }else{
            model.addAttribute("codeMsg","验证码不正确");
        }
        return "/site/forget";
    }

}
