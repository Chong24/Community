package com.nowcoder.community.util;

/**
 * 用来判断用户的状态，从而判断是否能激活成功，主要分为以下三种情况：
 * 1、激活成功
 * 2、重复激活
 * 3、激活失败
 * @author wang
 * @create 2022-03-15
 */
public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 2;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
}
