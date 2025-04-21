package ru.mishazx.systemotpjava.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки уведомлений через различные каналы (SMS, Email, Telegram).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    
    /**
     * Отправляет OTP код по SMS.
     *
     * @param phoneNumber номер телефона получателя
     * @param otpCode код для отправки
     * @return true если отправка успешна
     */
    public boolean sendSmsOtp(String phoneNumber, String otpCode) {
        try {
            // В реальной системе здесь должна быть интеграция с SMS-провайдером
            log.info("Sending SMS OTP to {}: {}", phoneNumber, otpCode);
            
            // Пример реализации с использованием API SMS-провайдера:
            // smsApiClient.sendMessage(phoneNumber, "Ваш код подтверждения: " + otpCode);
            
            // Для демонстрации считаем, что SMS успешно отправлена
            return true;
        } catch (Exception e) {
            log.error("Error sending SMS OTP", e);
            return false;
        }
    }
    
    /**
     * Отправляет OTP код по электронной почте.
     *
     * @param email адрес электронной почты получателя
     * @param otpCode код для отправки
     * @return true если отправка успешна
     */
    public boolean sendEmailOtp(String email, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Код подтверждения");
            message.setText("Ваш код подтверждения: " + otpCode + "\n\nКод действителен в течение 5 минут.");
            
            mailSender.send(message);
            log.info("Email OTP sent to: {}: {}", email, otpCode);
            return true;
        } catch (Exception e) {
            log.error("Error sending email OTP", e);
            return false;
        }
    }
    
    /**
     * Отправляет OTP код через Telegram.
     *
     * @param telegramUsername имя пользователя в Telegram
     * @param otpCode код для отправки
     * @return true если отправка успешна
     */
    public boolean sendTelegramOtp(String telegramUsername, String otpCode) {
        try {
            // В реальной системе здесь должна быть интеграция с Telegram Bot API
            log.info("Sending Telegram OTP to {}: {}", telegramUsername, otpCode);
            
            // Пример реализации с использованием Bot API:
            // telegramBot.sendMessage(telegramUsername, "Ваш код подтверждения: " + otpCode);
            
            // Для демонстрации считаем, что сообщение успешно отправлено
            return true;
        } catch (Exception e) {
            log.error("Error sending Telegram OTP", e);
            return false;
        }
    }
} 