package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;


    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @GetMapping("/share")
    @ResponseBody
    public String share(String htmlUrl){
        //文件名,用随机字符串防止重名
        String fileName = CommunityUtil.generateUUID();

        //异步生成长图，用消息队列完成异步的方式
        Event event = new Event().setTopic(TOPIC_SHARE).setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName).setData("suffix",".png");
        eventProducer.fireEvent(event);

        //返回访问路径，用于给浏览器能访问
        Map<String,Object> map = new HashMap<>();
//        map.put("shareUrl",domain + contextPath + "/share/image/" + fileName);
        map.put("shareUrl", shareBucketUrl + "/" + fileName);
        return CommunityUtil.getJSONString(0,null,map);
    }

    //废弃，不再从本地读，改为从云服务器直接读
    //获取长图
    @GetMapping("/share/image/{fileName}")
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response){
        if (StringUtils.isBlank(fileName)){
            throw new IllegalArgumentException("文件名不能为空");
        }
        //设置response输出格式
        response.setContentType("image/png");
        //访问文件
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败：" + e.getMessage());
        }
    }
}
