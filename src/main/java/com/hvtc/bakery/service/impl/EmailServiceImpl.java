package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:xyociu@gmail.com}")
    private String fromEmail;

    @Override
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            log.info("Bắt đầu gửi email chào mừng tới: {}", toEmail);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Chào mừng bạn đến với HVTC BAKERY - Mã giảm giá chào mừng!");
            
            String greetingName = (fullName != null && !fullName.isBlank()) ? fullName : "bạn";
            String content = "Kính gửi " + greetingName + ",\n\n"
                    + "Chào mừng bạn đến với HVTC BAKERY! Mã giảm giá 10% cho đơn đặt bánh đầu tiên của bạn là: HVTC\n\n"
                    + "Chúc bạn có trải nghiệm tuyệt vời cùng những chiếc bánh tươi ngon của HVTC BAKERY!\n\n"
                    + "Trân trọng,\n"
                    + "Đội ngũ HVTC BAKERY";
                    
            message.setText(content);

            mailSender.send(message);
            log.info("Đã gửi email chào mừng thành công tới địa chỉ: {}", toEmail);
        } catch (Exception ex) {
            log.error("Lỗi khi gửi email chào mừng tới {}: {}", toEmail, ex.getMessage(), ex);
        }
    }
}
