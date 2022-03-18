package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * 对LoginTicket表的操作
 * 本次是用注解的方式查询，也可以用以前的xml映射文件查询
 * 复杂查询可以用映射文件，简单的用注解
 * @author wang
 * @create 2022-03-16
 */
@Mapper
public interface LoginTicketMapper {
    /**
     * 插入到login_ticket表格中，即登陆成功之后，需要在服务器存一个登录凭证的数据
     * @param loginTicket
     * @return
     */
    @Insert({"insert into login_ticket(user_id,ticket,status,expired) values(#{userId},#{ticket},#{status},#{expired})"})
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 根据登录凭证查出数据，因为登陆凭证ticket才是这个表的核心字段
     * @param ticket
     * @return
     */
    @Select({"select id,user_id,ticket,status,expired from login_ticket where ticket = #{ticket}"})
    LoginTicket selectByTicket(String ticket);

    /**
     * 登录成功后，拿到登陆凭证，需要更新用户的状态
     * @param ticket
     * @param status
     * @return
     */
    @Update({"update login_ticket set status = #{status} where ticket = #{ticket}"})
    int updateStatus(@Param("ticket") String ticket, @Param("status") int status);
}
