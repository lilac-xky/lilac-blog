package com.lilac.manager.email;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 邮件服务
 */
@Service
@Slf4j
public class MailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送普通文本邮件（异步执行）
     *
     * @param to      接收者
     * @param subject 主题
     * @param content 内容
     */
    @Async("mailExecutor")
    public void sendSimpleMail(String to, String subject, String content) {
        log.info("开始异步发送邮件至: {}", to);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("邮件发送成功: {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败，接收人: {}, 错误信息: {}", to, e.getMessage());
        }
    }
}