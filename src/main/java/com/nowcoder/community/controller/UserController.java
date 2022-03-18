package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.Impl.UserServiceImpl;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 处理一些用户设置，修改头像密码等
 * @author wang
 * @create 2022-03-17
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 上传头像：上传就是将本地的文件传到服务器文件下
     * @param headerImage：MultipartFile是处理单个文件的，多个文件需要用MultipartFile[]
     * @param model
     * @return
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }
        
        //获取文件的格式
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }

        //确定文件存放在服务器的路径，为了避免每次上传文件名一样产生覆盖，选用随机字符串当文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        File dest = new File(uploadPath+"/"+fileName);

        //用流上传
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        //更新当前头像路径（web访问路径）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    /**
     * 显示头像
     * @param fileName 文件名
     * @param response  由于输出的是特殊格式，是图片，需要自己输出
     */
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //文件的输入输出流
        FileInputStream fis = null;
        OutputStream os = null;

        //服务器存头像的路径
        fileName = uploadPath + "/" +fileName;
        // 获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片：固定格式
        response.setContentType("image/" + suffix);

        //需要流，图片是二进制，需要字节流
        try {
            fis = new FileInputStream(fileName);
            os = response.getOutputStream();
            //需要读文件的区间步长，即缓冲流
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @LoginRequired
    @PostMapping ("/updatePassword/{id}")
    public String updatePassword(@PathVariable("id") int id, String oldPassword, String newPassword, String confirmPassword, Model model){
        User user = userService.selectById(id);
        if (user == null){
            model.addAttribute("userMsg","请先登录");
            return "/site/setting";
        }
        String s = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!s.equals(user.getPassword())){
            model.addAttribute("userMsg","密码错误");
            return "/site/setting";
        }
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("confirmPasswordMsg","密码不一致");
            return "/site/setting";
        }
        String s1 = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(id,s1);
        model.addAttribute("userMsg","修改成功");
        return "/site/setting";
    }
}