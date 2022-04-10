package com.nowcoder.community.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class WKImageDeleteJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(WKImageDeleteJob.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    //真正删除生成长图的逻辑
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //获取wkImageStorage目录下的所有生成长图的文件
        File[] files = new File(wkImageStorage).listFiles();
        if (files == null || files.length == 0){
            logger.info("没有WK图片，任务取消！");
            return;
        }
        for(File file : files){
            //为了防止删除刚生成的图片，所以我们用时间戳做个判断，删除一分钟前创建的图片，一分钟内创建的就不删除
            if (System.currentTimeMillis() - file.lastModified() > 60 * 1000){
                logger.info("删除WK图片：" + file.getName());
                file.delete();
            }
        }
    }
}
