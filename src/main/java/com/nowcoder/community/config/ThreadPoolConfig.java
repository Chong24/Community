package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * spring线程池的配置:
 * @EnableScheduling：该注解标注在配置类上，用于开启对定时任务的支持。
 * @Scheduled：该注解标注在具体的方法上，用于声明具体要执行的定时任务。
 * @EnableAsync注解的意思是开启支持异步，就是开启支持多线程的意思。可以标注在方法、类上。
 * 使用@Async注解的时候一定要在类上加@EnableAsync（注解形式），代表具体的异步操作，
 * 调用标了@Async的方法，线程池就会分一个线程去处理
 */

@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
