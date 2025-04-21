package ru.mishazx.systemotpjava.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Сервис для доставки OTP-кодов различными способами.
 * Поддерживает отправку по Email, SMS, через Telegram или сохранение в файл.
 */
@Service
@Slf4j
public class OTPDeliveryService {
    
    @Value("${app.otp.file-storage-path:./otp-codes}")
    private String fileStoragePath;
    
    private final JavaMailSender emailSender;
    // Здесь будут другие зависимости для SMS и Telegram
    
    @Autowired
    public OTPDeliveryService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }
    
    /**
     * Отправляет OTP-код через выбранный канал доставки.
     * 
     * @param userId ID пользователя
     * @param code OTP-код
     * @param deliveryMethod Метод доставки (EMAIL, SMS, TELEGRAM, FILE)
     * @param deliveryAddress Адрес доставки
     * @return true, если доставка успешна
     */
    public boolean sendOtp(String userId, String code, String deliveryMethod, String deliveryAddress) {
        try {
            switch (deliveryMethod.toUpperCase()) {
                case "EMAIL":
                    return sendByEmail(deliveryAddress, code);
                case "SMS":
                    return sendBySms(deliveryAddress, code);
                case "TELEGRAM":
                    return sendByTelegram(deliveryAddress, code);
                case "FILE":
                    return saveToFile(userId, code);
                default:
                    log.error("Unsupported delivery method: {}", deliveryMethod);
                    return false;
            }
        } catch (Exception e) {
            log.error("Failed to deliver OTP code via {}: {}", deliveryMethod, e.getMessage());
            return false;
        }
    }
    
    /**
     * Отправляет код по электронной почте.
     */
    private boolean sendByEmail(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Ваш OTP-код для подтверждения");
            message.setText("Ваш код подтверждения: " + code + "\n" +
                    "Код действителен в течение ограниченного времени.");
            
            emailSender.send(message);
            log.info("OTP sent via email to {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Отправляет код через SMS (эмуляция).
     */
    private boolean sendBySms(String phoneNumber, String code) {
        // Здесь будет интеграция с SMPP-сервером
        log.info("OTP sent via SMS to {} (emulated): {}", phoneNumber, code);
        return true;
    }
    
    /**
     * Отправляет код через Telegram.
     */
    private boolean sendByTelegram(String telegramId, String code) {
        // Здесь будет интеграция с Telegram Bot API
        log.info("OTP sent via Telegram to {} (emulated): {}", telegramId, code);
        return true;
    }
    
    /**
     * Сохраняет код в файл.
     */
    private boolean saveToFile(String userId, String code) throws IOException {
        Path directory = Paths.get(fileStoragePath);
        Files.createDirectories(directory);
        
        Path filePath = directory.resolve(userId + ".txt");
        
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        String content = String.format("[%s] OTP Code: %s%n", timestamp, code);
        
        Files.write(
            filePath, 
            content.getBytes(), 
            StandardOpenOption.CREATE, 
            StandardOpenOption.APPEND
        );
        
        log.info("OTP saved to file for user: {}", userId);
        return true;
    }
} 