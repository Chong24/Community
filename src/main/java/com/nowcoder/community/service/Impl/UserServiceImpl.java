package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 提供对用户的数据库查询服务
 * @author wang
 * @create 2022-03-10
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //由于查询改从缓存中查，所以对用户的更新操作我们要清除缓存，让其下一次调用selectById方法，开始redis缓存初始化时更新新的值
    @Override
    public User selectById(int id) {
        //从缓存中获取
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    @Override
    public User selectByName(String username) {
        return userMapper.selectByName(username);
    }

    @Override
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    @Override
    public int updateStatus(int id, int status) {
        return userMapper.updateStatus(id,status);
    }

    @Override
    public int updateHeader(int id, String headerUrl) {

//        return userMapper.updateHeader(id,headerUrl);
        int rows = userMapper.updateHeader(id, headerUrl);
        clearCache(id);
        return rows;
    }

    @Override
    public int updatePassword(int id, String password) {

//        return userMapper.updatePassword(id,password);
        int rows = userMapper.updatePassword(id, password);
        clearCache(id);
        return rows;
    }

    //缓存的三步
    //1、优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        //User之所以还能用String的redis类型存，是因为它会根据类型序列化
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //2、取不到时则查mysql，然后初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    //3、数据变更时清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
