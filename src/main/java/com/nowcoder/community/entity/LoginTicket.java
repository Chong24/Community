package com.nowcoder.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 用于关联数据库的login_ticket表格
 * 作用：当用户登录成功之后，服务器会生成一个登陆凭证给浏览器，存在cookie中，记录是该用户登录的状态
 * 注销：即让这个凭证过期
 * @author wang
 * @create 2022-03-16
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginTicket {

    private int id;
    private int userId;         //用户id
    private String ticket;      //登录凭证，即存在cookie里的数据，用来使请求有状态，即有关联性
    private int status;         //状态，0代表登录，1代表注销
    private Date expired;       //过期时间，即cookie过期时间，过了这个时间，就不认为是同一个用户在操作

    public LoginTicket(int userId, String ticket, int status, Date expired) {
        this.userId = userId;
        this.ticket = ticket;
        this.status = status;
        this.expired = expired;
    }
}
