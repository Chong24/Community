package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.lang.reflect.Member;

/**
 * 模拟邮件客户端——例如新浪等
 * @author wang
 * @create 2022-03-11
 */
@Component
public class MailClient {

    //需要记录日志信息，如果邮件发送失败，可以进行很好的提醒
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    //用spring封装好的专门处理邮件的jar包来发送，需要导入依赖
    @Autowired
    private JavaMailSender mailSender;

    //用value注解传入发送邮件的用户名
    @Value("${spring.mail.username}")
    private String from;

    /**
     *
     * @param to 发给谁
     * @param subject 发的邮件标题
     * @param content 发的邮件内容
     */
    public void sendMail(String to, String subject, String content){
        try {
            //创建出邮件实例
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            //设置邮件内容
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);
            //发送邮件
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            //打印日志信息
            logger.error("发送邮件失败：" + e.getMessage());
        }
    }
}
