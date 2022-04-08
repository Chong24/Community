package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.Impl.DiscussPostServiceImpl;
import com.nowcoder.community.service.Impl.ElasticsearchServiceImpl;
import com.nowcoder.community.service.Impl.LikeServiceImpl;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 由于每次点赞、评论、加精等操作都会影响分数，即影响最热排行榜的名次。
 * 但这些都是常用操作，操作量大，如果每次发生变化就计算一次分数，性能就很低。
 * 因此，我们使用quartz的定时功能，每过5分钟我们就集中计算一次更新的分数。
 * 因此，在这五分钟内，如果有分数发生了变化，我们就把它存在redis缓存中，等时间一到，我们就更新数据库的分数
 * @author wang
 * @create 2022-04-07
 */
public class PostScoreRefreshJob implements Job, CommunityConstant{

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private LikeServiceImpl likeService;

    @Autowired
    private ElasticsearchServiceImpl elasticsearchService;

    // 牛客纪元，这个是一个常量，因此我们只需要加载一次就可以，所以用静态代码块完成解析操作后加载到常量池中
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();

        //获取到key为redisKey的value数据，代表需要刷新分数的帖子
        //由于可能同一个帖子被多次点赞，而我们只需要算一次分数，所以我们存在redis的set集合中，自动去重
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0){
            logger.info("任务取消，没有需要刷新的帖子");
            return;
        }
        logger.info("任务开始 正在刷新帖子分数：" + operations.size());
        while (operations.size() > 0){
            //真正计算分数的操作
            this.refresh((Integer) operations.pop());
        }
        logger.info("任务结束 帖子分数刷新完毕！");
    }

    private void refresh(Integer postId) {
        DiscussPost post = discussPostService.selectDiscussPostById(postId);
        if (post == null){
            logger.error("该帖子不存在：id = " + postId);
            return;
        }

        //是否加精
        boolean wonderful =  post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //分数 = 帖子权重 + 距离天数，以天为单位，分数要保证大于等于0
        double score = Math.log10(Math.max(w,1)) + (post.getCreateTime().getTime() - epoch.getTime())/(1000*3600*24);
        //更新帖子分数
        discussPostService.updateScore(postId,score);
        //同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
