package com.nowcoder.community.service;

import com.nowcoder.community.entity.LoginTicket;

/**
 * @author wang
 * @create 2022-03-16
 */
public interface LoginTicketService {

    int insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectByTicket(String ticket);

    int updateStatus(String ticket,int status);
}
