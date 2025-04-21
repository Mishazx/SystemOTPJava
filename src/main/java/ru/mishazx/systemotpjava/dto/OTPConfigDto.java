package ru.mishazx.systemotpjava.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OTPConfigDto {
    private Integer codeLength;
    private Integer lifetimeMinutes;
    private Integer maxAttempts;
    private Boolean resendingEnabled;
    private Integer resendIntervalSeconds;
    
    // Constructor for minimal configuration
    public OTPConfigDto(Integer codeLength, Integer lifetimeMinutes) {
        this.codeLength = codeLength;
        this.lifetimeMinutes = lifetimeMinutes;
    }
}
