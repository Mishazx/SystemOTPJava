package ru.mishazx.systemotpjava.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import ru.mishazx.systemotpjava.dto.OtpRequestDto;
import ru.mishazx.systemotpjava.dto.OtpResponseDto;
import ru.mishazx.systemotpjava.dto.OtpValidationRequestDto;
import ru.mishazx.systemotpjava.dto.OtpValidationResponseDto;
import ru.mishazx.systemotpjava.services.OTPDeliveryService;
import ru.mishazx.systemotpjava.services.OTPService;

/**
 * Контроллер для работы с OTP-кодами.
 * Предоставляет API для генерации и валидации OTP кодов.
 */
@RestController
@RequestMapping("/api/otp")
@Slf4j
@PreAuthorize("isAuthenticated()")
public class OTPController {
    private final OTPService otpService;
    private final OTPDeliveryService deliveryService;

    public OTPController(OTPService otpService, OTPDeliveryService deliveryService) {
        this.otpService = otpService;
        this.deliveryService = deliveryService;
    }

    /**
     * Генерирует OTP-код и отправляет его через выбранный канал доставки.
     * 
     * @param request DTO с параметрами запроса (канал доставки, адрес, идентификатор операции)
     * @param authentication Данные аутентифицированного пользователя
     * @return Информация о сгенерированном OTP-коде
     */
    @PostMapping("/generate")
    public ResponseEntity<OtpResponseDto> generateOtp(
            @RequestBody OtpRequestDto request,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Generating OTP for user: {}, delivery method: {}", userId, request.getDeliveryMethod());
        
        // Генерация кода
        String code = otpService.generateOtp(
                userId, 
                request.getDeliveryMethod(),
                request.getDeliveryAddress(),
                request.getOperationId());
        
        // Отправка кода по выбранному каналу
        boolean delivered = deliveryService.sendOtp(
                userId, 
                code, 
                request.getDeliveryMethod(),
                request.getDeliveryAddress());
        
        OtpResponseDto response = new OtpResponseDto();
        response.setSuccess(delivered);
        response.setMessage(delivered ? 
                "OTP code has been sent successfully" : 
                "Failed to deliver OTP code");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Проверяет валидность OTP-кода.
     * 
     * @param request DTO с параметрами запроса (код, идентификатор операции)
     * @param authentication Данные аутентифицированного пользователя
     * @return Результат валидации OTP-кода
     */
    @PostMapping("/validate")
    public ResponseEntity<OtpValidationResponseDto> validateOtp(
            @RequestBody OtpValidationRequestDto request,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Validating OTP for user: {}, operation: {}", userId, request.getOperationId());
        
        boolean isValid = otpService.validateOtp(userId, request.getCode(), request.getOperationId());
        
        OtpValidationResponseDto response = new OtpValidationResponseDto();
        response.setValid(isValid);
        response.setMessage(isValid ? 
                "OTP code is valid" : 
                "Invalid or expired OTP code");
        
        return ResponseEntity.ok(response);
    }
} 