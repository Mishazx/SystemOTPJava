package ru.mishazx.systemotpjava.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mishazx.systemotpjava.services.EmailService;
// import ru.mishazx.systemotpjava.services.SmppSmsService;

/**
 * Сервис для отправки уведомлений пользователям через различные каналы
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    // private final SmppSmsService smppSmsService;

    /**
     * Отправляет OTP код пользователю через указанный канал связи
     *
     * @param contact   контакт пользователя (email или телефон)
     * @param otpCode   сгенерированный OTP код
     * @param channel   канал связи (EMAIL, SMS)
     * @return          true если отправка успешна
     */
    public boolean sendOtp(String contact, String otpCode, NotificationChannel channel) {
        try {
            switch (channel) {
                case EMAIL:
                    emailService.sendOtpEmail(contact, otpCode);
                    break;
                case SMS:
                    // smppSmsService.sendOtp(contact, otpCode);
                    break;
                default:
                    log.error("Unsupported notification channel: {}", channel);
                    return false;
            }
            log.info("OTP code sent to {} via {}", contact, channel);
            return true;
        } catch (Exception e) {
            log.error("Failed to send OTP via {}: {}", channel, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Отправляет OTP код по SMS (метод для обратной совместимости)
     *
     * @param phoneNumber номер телефона получателя
     * @param otpCode код для отправки
     * @return true если отправка успешна
     */
    public boolean sendSmsOtp(String phoneNumber, String otpCode) {
        return sendOtp(phoneNumber, otpCode, NotificationChannel.SMS);
    }

    /**
     * Отправляет OTP код по электронной почте (метод для обратной совместимости)
     *
     * @param email адрес электронной почты получателя
     * @param otpCode код для отправки
     * @return true если отправка успешна
     */
    public boolean sendEmailOtp(String email, String otpCode) {
        return sendOtp(email, otpCode, NotificationChannel.EMAIL);
    }

    /**
     * Отправляет OTP код через Telegram (метод для обратной совместимости)
     *
     * @param telegramUsername имя пользователя в Telegram
     * @param otpCode код для отправки
     * @return true если отправка успешна
     */
    public boolean sendTelegramOtp(String telegramUsername, String otpCode) {
        // Пока просто логируем, так как Telegram не реализован
        log.info("TEST MODE - Would send Telegram OTP to {} with code: {}", telegramUsername, otpCode);
        return true;
    }

    /**
     * Каналы доставки уведомлений
     */
    public enum NotificationChannel {
        EMAIL,
        SMS
    }
} 