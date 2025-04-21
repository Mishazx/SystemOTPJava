package ru.mishazx.systemotpjava.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "otp_config")
@Data
public class OTPConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer codeLength = 6; // Количество цифр в коде, по умолчанию 6
    
    @Column(nullable = false)
    private Integer lifetimeMinutes = 15; // Время жизни в минутах, по умолчанию 15

    @Column(nullable = false)
    private Integer maxAttempts = 3;

    @Column(nullable = false)
    private Boolean resendingEnabled = true; // Флаг, включена ли повторная отправка кода

    @Column(nullable = false)
    private Integer resendIntervalSeconds = 10; // Минимальный интервал между повторными отправками (в секундах)
}
