package ru.mishazx.systemotpjava.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса генерации OTP-кода.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequestDto {
    // Метод доставки (EMAIL, SMS, TELEGRAM, FILE)
    private String deliveryMethod;
    
    // Адрес доставки (email, номер телефона, telegram id)
    private String deliveryAddress;
    
    // Идентификатор операции (опционально)
    private String operationId;
} 