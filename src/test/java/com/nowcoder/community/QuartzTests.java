package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * 因为我们对quartz做了配置，将JobDetail、Trigger都注入到了spring的IOC容器中，
 * 只要一启动程序，就会自动执行AlphaJob的execute方法，完成这个工作job。
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class QuartzTests {

    //调度容器
    @Autowired
    private Scheduler scheduler;

    //删除quartz存在数据库的数据
    @Test
    public void testDeleteJob(){
        try {
            boolean result = scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
            System.out.println(result);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
