package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * 主要功能：提供一个工具类，可以生成随机字符串，用于密码的加密
 * @author wang
 * @create 2022-03-15
 */
public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID(){
        //随机生成的UUID可能会随机出“-”，我们不需要
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * 对密码进行加密
     * 例如：将hello  ->  加密成abc123def456abc，但由于这种简单的加密很容易被破解，
     * 所以我们的方法是加上随机生成给的字符串，然后带上这字符串一起加密
     * @param key  需要加密的密码
     * @return
     */
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        //DigestUtils是一个加密算法工具类，常用的加密算法就是md5。
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 将数据封装为Json返回
     * @param code 返回的和用户交互状态码
     * @param msg 返回和用户交互的信息
     * @param map 需要显示的数据内容
     * @return
     */
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map != null){
            for (String key : map.keySet()){
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code,msg,null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code,null,null);
    }
}
