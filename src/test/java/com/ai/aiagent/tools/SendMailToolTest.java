package com.ai.aiagent.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("local")
class SendMailToolTest {
    @Resource
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String senderEmail;
    @Test
    void sendMail() {
        String from = senderEmail;
        String to = "wy163stxh@163.com";
        String title = "Test Email";
        String context = "This is a test email content";
        SendMailTool sendMailTool = new SendMailTool(mailSender);
        String result = sendMailTool.sendMail(from, to, title, context);
        assertNotNull(result);
    }
}