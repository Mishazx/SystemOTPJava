package ru.mishazx.systemotpjava.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import ru.mishazx.systemotpjava.dto.OTPConfigDto;
import ru.mishazx.systemotpjava.dto.UserDto;
import ru.mishazx.systemotpjava.exceptions.IllegalOperationException;
import ru.mishazx.systemotpjava.exceptions.UserNotFoundException;
import ru.mishazx.systemotpjava.models.OTPConfig;
import ru.mishazx.systemotpjava.models.User;
import ru.mishazx.systemotpjava.services.AdminUserService;


@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {
    private final AdminUserService adminUserService;

    @Autowired
    public AdminController(AdminUserService adminUserService) {
        // this.otpConfigService = otpConfigService;
        this.adminUserService = adminUserService;
    }

    // Получить текущую конфигурацию OTP
    @GetMapping("/otp-config")
    public ResponseEntity<OTPConfigDto> getOtpConfig() {
        OTPConfig config = adminUserService.getCurrentConfig();
        OTPConfigDto dto = new OTPConfigDto(config.getCodeLength(), config.getLifetimeMinutes());

        log.info("Admin requested OTP config: {}", dto);
        return ResponseEntity.ok(dto);
    }

    // Обновить конфигурацию OTP
    @PutMapping("/otp-config")
    public ResponseEntity<OTPConfigDto> updateOtpConfig(@RequestBody OTPConfigDto configDto) {
        log.info("Admin updating OTP config: {}", configDto);

        OTPConfig updatedConfig = adminUserService.updateConfig(
                configDto.getCodeLength(),
                configDto.getLifetimeMinutes()
        );

        OTPConfigDto responseDto = new OTPConfigDto(
                updatedConfig.getCodeLength(),
                updatedConfig.getLifetimeMinutes()
        );

        return ResponseEntity.ok(responseDto);
    }

    // Получить список всех не-админов
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllNonAdminUsers() {
        List<User> users = adminUserService.getNonAdminUsers();

        List<UserDto> userDtos = users.stream()
                .map(user -> new UserDto(
                ))
                .collect(Collectors.toList());

        log.info("Admin requested list of non-admin users, count: {}", userDtos.size());
        return ResponseEntity.ok(userDtos);
    }

    // Удалить пользователя
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("Admin requested deletion of user: {}", userId);

        try {
            adminUserService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            log.warn("User not found for deletion: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (IllegalOperationException e) {
            log.warn("Illegal operation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}