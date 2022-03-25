package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 统一的异常处理
 * @ControllerAdvice是@Controller的增强器，一般与@ExceptionHandler搭配处理异常
 * @author wang
 * @create 2022-03-23
 */

//代表只扫描标了@Controller注解的方法
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    //参数代表能处理的异常类型，Exception是异常的顶级父类
    @ExceptionHandler({Exception.class})
    public void handlerException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        //返回与用户的交互信息，返回浏览器可以接收的数据：例如xml和html

        //通过请求头信息获得浏览器期望接收的数据
        String xRequestedWith = request.getHeader("x-requested-with");
        //如果是xml，我们就返回json
        if ("XMLHttpRequest".equals(xRequestedWith)){
            //设置返回给浏览器的格式
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));
        }else{
            //否则，我们就返回html页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
