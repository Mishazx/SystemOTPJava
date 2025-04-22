package ru.mishazx.systemotpjava.services;

// import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.core.env.Environment;

/**
 * Сервис для отправки Email сообщений
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final Environment environment;

    /**
     * Отправляет email с OTP кодом для подтверждения
     *
     * @param to       адрес получателя
     * @param otpCode  OTP код для подтверждения
     */
    public void sendOtpEmail(String to, String otpCode) {
        // Проверяем, настроен ли почтовый сервер
        String username = environment.getProperty("spring.mail.username");
        String password = environment.getProperty("spring.mail.password");
        
        log.info("Mail config: username={}, password is {}",
            username, 
            (password == null ? "NULL" : "SET"));

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            // Если не настроен - просто логируем код в консоль (для разработки)
            log.info("TEST MODE - Would send email to {} with OTP code: {}", to, otpCode);
            log.info("Configure spring.mail.* properties to enable actual email sending");
            return;
        }
        
        try {
            Context context = new Context();
            context.setVariable("otpCode", otpCode);

            String subject = "Код подтверждения";
            String content = templateEngine.process("email/otp-template", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(username); // используем имя пользователя из конфигурации как обратный адрес
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(mimeMessage);
            log.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage(), e);
        }
    }
} 