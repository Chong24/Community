package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis配置类，即配置RedisTemplate
 * @author wang
 * @create 2022-03-23
 */
@Configuration
public class RedisConfig {
    /**
     * 像容器装配redisTemplate，Springboot给我们配置了redisTemplate，但是key-vakue都是object类型的
     * 我们希望key是String，Value是Object类型的。
     * springboot看到参数需要依赖其他组件，如果IOC容器中有，会自动注入
     * @param factory redis连接工厂，只有连接上了才能操作redis
     * @return
     */
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        //连接redis
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        //因为redis存key-value是要进行序列化的，我们可以设置自己的序列化方式，要不然会采用默认的序列化方式，存在redis可能乱码

        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        //非spring注入使用RedisTemplate 需要先调用afterPropertiesSet方法,此方法是初始化参数和初始化工作。
        template.afterPropertiesSet();
        return template;
    }
}
