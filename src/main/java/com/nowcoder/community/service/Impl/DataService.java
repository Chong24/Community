package com.nowcoder.community.service.Impl;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * redis中存了一天登录了多少用户访问、有多少独立用户访问，
 * 一段时间内有多少用户访问、有多少独立用户访问。
 */

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    //对日期格式化当作redis的key
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    //一天：将指定的IP计入UV（UV是指独立用户的意思，即没有登录的用户访问也要记录），是通过ip地址记录的，一个IP算一次
    public void recordUV(String ip){
        //生成redis存的key
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        //HyperLogLog是redis的一种数据类型，可以去重，性能好、所占内存小，但是估算，准确率还挺高的
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    //一段时间：统计指定日期范围内的UV
    public Long calculateUV(Date start, Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //因为我们需要将一段时间中每天统计的ip次数统计到一个key中去存这段时间UV访问的次数，所以需要拿到这段时间每天存的key
        //整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        //获取日期类的示例，用于遍历该一段时间每天的key
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        //从start出发、不晚于end的所有日期
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        //合并这些数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(start),df.format(end));
        //List转为一维数组，可以直接用toArray()方法
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());

        //返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    //一天：将指定用户计入DAU（日活跃用户，需要登陆的才记录），
    //用Bitmap来存取，如果访问了就对应索引标为1（即true），0（即false），有很多位
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        //就是String，userId就是索引，访问了就把它置为1，所以可以进行位运算
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }

    //一段时间：统计指定日期范围内的DAU
    public long calculateDAU(Date start, Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //整合该日期范围内的key，一个key存的是一个bit数组，只有0、1组成
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(calendar.DATE, 1);
        }

        //进行or运算合并：代表这一段时间内只要访问过一次就算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
