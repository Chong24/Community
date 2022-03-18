package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * cookie的工具类，作用是获取cookie中指定key的值，
 * 即获取cookie中存的ticket，然后用这个ticket来查询是哪个用户
 * @author wang
 * @create 2022-03-17
 */
public class CookieUtil {

    /**
     * 获取存在cookie中指定key的value值
     * @param request
     * @param name
     * @return
     */
    public static String getValue(HttpServletRequest request,String name){
        //基本首先都需要对传入的形参做非空判断
        if(request == null || name == null){
            throw new IllegalArgumentException("参数为空");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null){
            for (Cookie cookie : cookies) {
                //要用equals
                if (cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
