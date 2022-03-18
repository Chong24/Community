package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;

import java.util.Map;

/**
 * @author wang
 * @create 2022-03-15
 */
public interface RegisterService {
    //注册
    Map<String,Object> register(User user);

    //激活
    int activation(int userId, String code);
}
