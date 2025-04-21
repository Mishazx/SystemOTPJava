package ru.mishazx.systemotpjava.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mishazx.systemotpjava.exceptions.IllegalOperationException;
import ru.mishazx.systemotpjava.exceptions.UserNotFoundException;
import ru.mishazx.systemotpjava.models.OTPConfig;
import ru.mishazx.systemotpjava.models.User;
import ru.mishazx.systemotpjava.repository.OTPConfigRepository;
import ru.mishazx.systemotpjava.repository.OTPRepository;
import ru.mishazx.systemotpjava.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
public class AdminUserService {
    private final UserRepository userRepository;
    private final OTPRepository otpRepository;
    private final OTPConfigRepository otpConfigRepository;

    @Autowired
    public AdminUserService(UserRepository userRepository, OTPRepository otpRepository, 
                           OTPConfigRepository otpConfigRepository) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.otpConfigRepository = otpConfigRepository;
    }

    // Получить всех пользователей кроме админов
    public List<User> getNonAdminUsers() {
        log.info("Fetching all non-admin users");
        return userRepository.findByRoleUsersNameRoleNot("ADMIN");
    }

    // Удалить пользователя и его OTP-коды
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Проверка, что удаляемый пользователь не админ
        if (user.getRoleUsers().stream().anyMatch(role -> "ADMIN".equals(role.getNameRole()))) {
            log.warn("Attempt to delete admin user: {}", userId);
            throw new IllegalOperationException("Cannot delete admin user");
        }

        // Удаление связанных OTP-кодов
        otpRepository.deleteByUserId(user.getId());
        log.info("Deleted OTP codes for user: {}", userId);

        // Удаление пользователя
        userRepository.delete(user);
        log.info("Deleted user: {}", userId);
    }
    
    // Получить текущую конфигурацию или создать с дефолтными значениями
    public OTPConfig getCurrentConfig() {
        return otpConfigRepository.findFirstBy()
                .orElseGet(() -> {
                    log.info("Creating default OTP configuration");
                    OTPConfig defaultConfig = new OTPConfig();
                    return otpConfigRepository.save(defaultConfig);
                });
    }
    
    // Обновить конфигурацию
    public OTPConfig updateConfig(Integer codeLength, Integer lifetimeMinutes) {
        OTPConfig config = getCurrentConfig();
        
        if (codeLength != null && codeLength >= 4 && codeLength <= 10) {
            config.setCodeLength(codeLength);
        }
        
        if (lifetimeMinutes != null && lifetimeMinutes > 0) {
            config.setLifetimeMinutes(lifetimeMinutes);
        }
        
        log.info("Updated OTP config: length={}, lifetime={}min", 
                config.getCodeLength(), config.getLifetimeMinutes());
        
        return otpConfigRepository.save(config);
    }
}