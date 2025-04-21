package ru.mishazx.systemotpjava.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Обработчик успешной аутентификации.
 * Перенаправляет пользователя на главную страницу, если OTP аутентификация пройдена,
 * или на страницу выбора метода OTP, если требуется двухфакторная аутентификация.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException {
        
        HttpSession session = request.getSession(false);
        boolean otpVerified = false;
        
        // Проверяем, есть ли в сессии флаг, что OTP верифицирован
        if (session != null) {
            Boolean verified = (Boolean) session.getAttribute("OTP_VERIFIED");
            otpVerified = verified != null && verified;
        }
        
        if (otpVerified) {
            // Если OTP проверен, перенаправляем на dashboard
            log.info("User {} authenticated with OTP, redirecting to dashboard", authentication.getName());
            response.sendRedirect("/dashboard");
        } else {
            // Если нет, перенаправляем на страницу выбора метода OTP
            log.info("User {} authenticated, redirecting to OTP method selection", authentication.getName());
            response.sendRedirect("/auth/otp/method");
        }
    }
} 