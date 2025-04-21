package ru.mishazx.systemotpjava.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mishazx.systemotpjava.models.OTPCode;
import ru.mishazx.systemotpjava.models.OTPConfig;
import ru.mishazx.systemotpjava.repository.OTPConfigRepository;
import ru.mishazx.systemotpjava.repository.OTPRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для работы с OTP-кодами: генерация, валидация, управление статусами.
 */
@Service
@Slf4j
public class OTPService {
    private final OTPRepository otpRepository;
    private final OTPConfigRepository otpConfigRepository;

    @Autowired
    public OTPService(OTPRepository otpRepository, OTPConfigRepository otpConfigRepository) {
        this.otpRepository = otpRepository;
        this.otpConfigRepository = otpConfigRepository;
    }

    /**
     * Генерирует OTP-код для пользователя.
     *
     * @param userId ID пользователя
     * @param deliveryMethod Метод доставки
     * @param deliveryAddress Адрес доставки
     * @param operationId ID операции (опционально)
     * @return Сгенерированный OTP-код
     */
    public String generateOtp(String userId, String deliveryMethod, String deliveryAddress, String operationId) {
        // Получаем текущую конфигурацию OTP
        OTPConfig config = getCurrentConfig();
        
        // Генерируем случайный код
        String code = generateRandomCode(config.getCodeLength());
        
        // Создаем новую запись OTP в базе
        OTPCode otpCode = new OTPCode();
        otpCode.setCode(code);
        otpCode.setUserId(Long.parseLong(userId));
        otpCode.setOperationId(operationId);
        otpCode.setStatus(OTPCode.OtpStatus.ACTIVE);
        otpCode.setCreatedAt(LocalDateTime.now());
        otpCode.setExpiresAt(LocalDateTime.now().plusMinutes(config.getLifetimeMinutes()));
        otpCode.setDeliveryMethod(OTPCode.DeliveryMethod.valueOf(deliveryMethod.toUpperCase()));
        otpCode.setDeliveryAddress(deliveryAddress);
        
        // Сохраняем код
        otpRepository.save(otpCode);
        log.info("Generated OTP code for user: {}, delivery method: {}", userId, deliveryMethod);
        
        return code;
    }
    
    /**
     * Проверяет валидность OTP-кода.
     *
     * @param userId ID пользователя
     * @param code OTP-код
     * @param operationId ID операции (опционально)
     * @return true, если код действителен
     */
    public boolean validateOtp(String userId, String code, String operationId) {
        // Находим активный код для пользователя
        Optional<OTPCode> otpOpt;
        
        if (operationId != null && !operationId.isEmpty()) {
            // Если указан ID операции, учитываем его при поиске
            otpOpt = otpRepository.findByUserIdAndCodeAndStatusAndOperationId(
                Long.parseLong(userId), code, OTPCode.OtpStatus.ACTIVE, operationId);
        } else {
            // Иначе ищем любой активный код
            otpOpt = otpRepository.findByUserIdAndCodeAndStatus(
                Long.parseLong(userId), code, OTPCode.OtpStatus.ACTIVE);
        }
        
        if (otpOpt.isPresent()) {
            OTPCode otp = otpOpt.get();
            
            if (otp.isExpired()) {
                // Если код просрочен, отмечаем это
                otp.markAsExpired();
                otpRepository.save(otp);
                log.warn("OTP code is expired for user: {}", userId);
                return false;
            }
            
            // Код верный и не просрочен - отмечаем как использованный
            otp.markAsUsed();
            otpRepository.save(otp);
            log.info("OTP code successfully validated for user: {}", userId);
            return true;
        }
        
        log.warn("Invalid OTP code provided for user: {}", userId);
        return false;
    }
    
    /**
     * Помечает все истекшие коды как EXPIRED.
     * Этот метод обычно вызывается по расписанию.
     */
    public void markExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        int count = otpRepository.markExpiredCodes(now);
        log.info("Marked {} expired OTP codes", count);
    }
    
    /**
     * Получает текущую конфигурацию OTP или создает с дефолтными значениями.
     */
    private OTPConfig getCurrentConfig() {
        return otpConfigRepository.findFirstBy()
                .orElseGet(() -> {
                    log.info("Creating default OTP configuration");
                    OTPConfig defaultConfig = new OTPConfig();
                    return otpConfigRepository.save(defaultConfig);
                });
    }
    
    /**
     * Генерирует случайный цифровой код заданной длины.
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10)); // Добавляем случайную цифру от 0 до 9
        }
        
        return code.toString();
    }
}