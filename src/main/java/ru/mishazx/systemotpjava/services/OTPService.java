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
     * @param username Имя пользователя
     * @param deliveryMethod Метод доставки
     * @param deliveryAddress Адрес доставки
     * @param operationId ID операции (опционально)
     * @return Сгенерированный OTP-код
     */
    public String generateOtp(String username, String deliveryMethod, String deliveryAddress, String operationId) {
        // Получаем текущую конфигурацию OTP
        OTPConfig config = getCurrentConfig();
        
        // Генерируем случайный код
        String code = generateRandomCode(config.getCodeLength());
        
        // Создаем новую запись OTP в базе
        OTPCode otpCode = new OTPCode();
        otpCode.setCode(code);
        
        // Используем хеш-код имени пользователя в качестве идентификатора
        // Это временное решение для обработки строковых идентификаторов
        Long userId = generateUserIdFromUsername(username);
        otpCode.setUserId(userId);
        
        otpCode.setOperationId(operationId);
        otpCode.setStatus(OTPCode.OtpStatus.ACTIVE);
        otpCode.setCreatedAt(LocalDateTime.now());
        otpCode.setExpiresAt(LocalDateTime.now().plusMinutes(config.getLifetimeMinutes()));
        otpCode.setDeliveryMethod(OTPCode.DeliveryMethod.valueOf(deliveryMethod.toUpperCase()));
        otpCode.setDeliveryAddress(deliveryAddress);
        
        // Сохраняем код
        otpRepository.save(otpCode);
        log.info("Generated OTP code for user: {}, delivery method: {}", username, deliveryMethod);
        
        return code;
    }
    
    /**
     * Проверяет валидность OTP-кода.
     *
     * @param username Имя пользователя
     * @param code OTP-код
     * @param operationId ID операции (опционально)
     * @return true, если код действителен
     */
    public boolean validateOtp(String username, String code, String operationId) {
        // Используем хеш-код имени пользователя
        Long userId = generateUserIdFromUsername(username);
        
        // Находим активный код для пользователя
        Optional<OTPCode> otpOpt;
        
        if (operationId != null && !operationId.isEmpty()) {
            // Если указан ID операции, учитываем его при поиске
            otpOpt = otpRepository.findByUserIdAndCodeAndStatusAndOperationId(
                userId, code, OTPCode.OtpStatus.ACTIVE, operationId);
        } else {
            // Иначе ищем любой активный код
            otpOpt = otpRepository.findByUserIdAndCodeAndStatus(
                userId, code, OTPCode.OtpStatus.ACTIVE);
        }
        
        if (otpOpt.isPresent()) {
            OTPCode otp = otpOpt.get();
            
            if (otp.isExpired()) {
                // Если код просрочен, отмечаем это
                otp.markAsExpired();
                otpRepository.save(otp);
                log.warn("OTP code is expired for user: {}", username);
                return false;
            }
            
            // Код верный и не просрочен - отмечаем как использованный
            otp.markAsUsed();
            otpRepository.save(otp);
            log.info("OTP code successfully validated for user: {}", username);
            return true;
        }
        
        log.warn("Invalid OTP code provided for user: {}", username);
        return false;
    }
    
    /**
     * Получает последний активный OTP код для пользователя.
     *
     * @param username Имя пользователя
     * @return Последний активный OTP код или null
     */
    public OTPCode getLastActiveOtp(String username) {
        Long userId = generateUserIdFromUsername(username);
        Optional<OTPCode> otpOpt = otpRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
            userId, OTPCode.OtpStatus.ACTIVE);
        
        return otpOpt.orElse(null);
    }
    
    /**
     * Помечает OTP код как устаревший.
     *
     * @param otpId ID кода
     * @return true, если успешно
     */
    public boolean markAsExpired(Long otpId) {
        Optional<OTPCode> otpOpt = otpRepository.findById(otpId);
        
        if (otpOpt.isPresent()) {
            OTPCode otp = otpOpt.get();
            otp.markAsExpired();
            otpRepository.save(otp);
            return true;
        }
        
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
    
    /**
     * Генерирует числовой ID на основе имени пользователя.
     * Использует хеш-код строки и преобразует его в положительное число.
     */
    private Long generateUserIdFromUsername(String username) {
        // Получаем хеш-код имени пользователя и преобразуем его в положительное число
        int hashCode = username.hashCode();
        long positiveHash = Math.abs((long) hashCode);
        
        // Ограничиваем длину числа (например, до 10 знаков)
        return positiveHash % 10000000000L;
    }
}