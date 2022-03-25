package com.nowcoder.community.service.Impl;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 点赞的业务层操作：需要redisTemplate;
 * 因为点赞的数据存的是redis中，查询就像map一样，所以不需要dao层
 * @author wang
 * @create 2022-03-24
 */
@Service
public class LikeServiceImpl {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞，点完赞需要更新赞的数量和用户主页的被点赞的数量（这两个是不可分的，所以需要事务操作）
     * @param userId        根据uerId查询是否已经点过赞了
     * @param entityType    实体类型，给什么类型点赞
     * @param entityId      给哪个点赞 ，这两个参数用于拼接key
     * @param entityUserId  实体的用户id，因为给帖子点赞，是给发帖子的那个用户点赞
     */
    public void like(int userId, int entityType, int entityId, int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                //查询语句要放在事务开始外面，因为放在事务开始后是不会执行的，
                // 是要等所有的语句组队成功提交后再按顺序一起执行
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                //开启事务
                operations.multi();
                if (isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                //提交事务
                return operations.exec();
            }
        });
    }

    /**
     * 查询点赞数量
     * @param entityType
     * @param entityId
     * @return
     */
    public Long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return 返回int是因为boolean只能表示两种状态，int能表示多种，为以后业务拓展做准备
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 : 0;
    }

    /**
     * 查询某个用户获得的赞
     * @param userId
     * @return
     */
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }

}
