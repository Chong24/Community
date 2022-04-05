package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * quartz的配置类：它是存在数据库中的，解决spring分布式定时任务可能存在的问题。
 * 配置 -> 数据库 -> 调用
 */
@Configuration
public class QuartzConfig {

    // FactoryBean可简化Bean的实例化过程:
    // 1.通过FactoryBean封装Bean的实例化过程.
    // 2.将FactoryBean装配到Spring容器里.
    // 3.将FactoryBean注入给其他的Bean.
    // 4.该Bean得到的是FactoryBean所管理的对象实例.

    //配置JobDetail：一个具体可执行的调度程序
    @Bean
    public JobDetailFactoryBean alphaJobDetail(){
        //通过JobDetailFactoryBean获取到JobDetail实例
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        //是否持久化
        factoryBean.setDurability(true);
        //是否可恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    //配置Trigger，（有SimpleTriggerFactoryBean、CoreTriggerFactoryBean两种选择）
    //参数alphaJobDetail会被spring自动注入，上面定义的。
    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
