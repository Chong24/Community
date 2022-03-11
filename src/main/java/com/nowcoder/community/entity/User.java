package com.nowcoder.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author wang
 * @create 2022-03-10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    private int id;
    private String username;        //用户Id
    private String password;        //用户密码
    private String salt;            //为了保护用户安全，在密码后加5位随机字符串并加密
    private String email;           //用户邮箱
    private int type;               //0-普通用户 1-超级用户
    private int status;             //0-未激活 1-已激活 2-拉黑
    private String activationCode;  //激活码
    private String headerUrl;       //头像地址
    private Date createTime;        //注册时间
}
