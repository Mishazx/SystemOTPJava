package ru.mishazx.systemotpjava.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса валидации OTP-кода.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpValidationRequestDto {
    // OTP-код для проверки
    private String code;
    
    // Идентификатор операции (опционально)
    private String operationId;
} 