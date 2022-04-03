package com.nowcoder.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用AOP统一记录日志信息，即能统一记日志又能解耦合
 * @author wang
 * @create 2022-03-23
 */

@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //excution是一个关键字，后面表示要切入方法的切入点
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //我们希望打印用户【ip】在【什么时间】访问了【com.nowcoder.community.service.xxx()】

        //首先我们要获得用户的ip，则需要拿到request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //以前都是通过控制器访问服务层，控制器就会有请求，有请求就有ip，是不会为空的，
        // 但是引用了kafka后，是异步的，可能获取的是消息队列中的数据，然后访问服务层，不通过控制器这条路，所以可能会为空
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();

        //获取时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        //获取切点的方法名  JoinPoint对象封装了SpringAop中切面方法的信息
        //	joinPoint.getSignature()获取封装了署名信息的对象,在该对象中可以获取到目标方法名,所属类的Class等信息
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        //打印日志
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip,now,target));
    }
}
