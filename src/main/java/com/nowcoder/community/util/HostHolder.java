package com.nowcoder.community.util;

/**
 * 考虑到线程安全，多并发问题，所以需要将各个线程的操作分开来，互不影响
 * 我们要显示用户的数据，那么必然要将查询出的用户存在map或model中，这当遇到高并发的问题时，容易出现问题
 * 所以我们用ThreadLocal来存取user，这个底层是先获取当前线程，然后再存在map中。确保线程只能取到与之对应的用户数据
 * @author wang
 * @create 2022-03-17
 */

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，代替session
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
