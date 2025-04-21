package ru.mishazx.systemotpjava.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа на запрос валидации OTP-кода.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpValidationResponseDto {
    // Действителен ли OTP-код
    private boolean valid;
    
    // Сообщение о результате проверки
    private String message;
} 