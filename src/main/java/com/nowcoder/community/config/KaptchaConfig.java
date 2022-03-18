package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 生成动态的验证码给登录页面
 * 即需要实现顶级接口Producer，有默认实现类DefaultKaptcha，我们只需要自定义配置这个类即可
 * @author wang
 * @create 2022-03-16
 */

@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kapychaProducer(){
        Properties properties = new Properties();
        //设置参数
        properties.setProperty("kaptcha.image.width", "100");   //图像长
        properties.setProperty("kaptcha.image.height", "40");   //宽
        properties.setProperty("kaptcha.textproducer.font.size", "32");     //字体大小
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0"); //字体颜色
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYAZ"); //验证码内容，随机选取字符串转为字符
        properties.setProperty("kaptcha.textproducer.char.length", "4");    //字符个数
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");   //图片扰动失真处理

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        //配置DefaultKaptcha，需要Config，Config没有无参构造器，需要传入一个配置文件，文件中是配置参数
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
