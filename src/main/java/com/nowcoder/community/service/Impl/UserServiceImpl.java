package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 提供对用户的数据库查询服务
 * @author wang
 * @create 2022-03-10
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User selectById(int id) {
        return userMapper.selectById(id);
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
        return userMapper.updateHeader(id,headerUrl);
    }

    @Override
    public int updatePassword(int id, String password) {
        return userMapper.updatePassword(id,password);
    }
}
