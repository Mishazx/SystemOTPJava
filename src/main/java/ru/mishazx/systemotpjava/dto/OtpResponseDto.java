package ru.mishazx.systemotpjava.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа на запрос генерации OTP-кода.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponseDto {
    // Успешно ли выполнена операция
    private boolean success;
    
    // Сообщение о результате операции
    private String message;
} 