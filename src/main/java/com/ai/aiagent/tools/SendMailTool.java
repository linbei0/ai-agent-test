package com.ai.aiagent.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SendMailTool {

    private JavaMailSender javaMailSender;

    //使用构造器注入
    public SendMailTool(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Tool(description = "Send an email with the specified sender, recipient, subject, and body", returnDirect = true)
    public String sendMail(
            @ToolParam(description = "The sender of the email") String from,
            @ToolParam(description = "The recipient of the email") String to,
            @ToolParam(description = "The subject line of the email") String title,
            @ToolParam(description = "The body content of the email") String context) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(title);
            message.setText(context);
            javaMailSender.send(message);
            return "email sent successfully to: " + to;
        } catch (Exception e) {
            return "Error sending eamil: " + e.getMessage();
        }
    }
}


