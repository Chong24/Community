package com.nowcoder.community.config;

import com.nowcoder.community.service.AlphaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class AlphaConfig {

    @Bean
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    //测试bean的生命周期——初始化和销毁的过程
//    @Bean(initMethod = "init",destroyMethod = "destroy")
//    public AlphaService alphaService(){
//        return new AlphaService();
//    }

}
