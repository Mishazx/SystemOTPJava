package ru.mishazx.systemotpjava.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для обработки корневого пути и перенаправления пользователей
 * в зависимости от статуса их аутентификации
 */
@Controller
@Slf4j
public class HomeController {

    /**
     * Обрабатывает запросы к корневому пути "/"
     * - Если пользователь полностью аутентифицирован (включая OTP) → перенаправляет на "/dashboard"
     * - Если пользователь аутентифицирован, но не прошел OTP → перенаправляет на "/auth/otp/method"
     * - Если пользователь не аутентифицирован → перенаправляет на "/login"
     */
    @GetMapping("/")
    public String index(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Проверяем, аутентифицирован ли пользователь
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            // Пользователь аутентифицирован, проверяем статус OTP
            HttpSession session = request.getSession(false);
            boolean otpVerified = false;
            
            if (session != null) {
                Boolean verified = (Boolean) session.getAttribute("OTP_VERIFIED");
                otpVerified = verified != null && verified;
            }
            
            if (otpVerified) {
                // Пользователь полностью аутентифицирован с OTP
                log.info("User {} fully authenticated (with OTP), redirecting to dashboard", authentication.getName());
                return "redirect:/dashboard";
            } else {
                // Пользователь аутентифицирован, но нужно OTP
                log.info("User {} authenticated but needs OTP, redirecting to OTP method selection", authentication.getName());
                return "redirect:/auth/otp/method";
            }
        } else {
            // Пользователь не аутентифицирован
            log.info("User not authenticated, redirecting to login");
            return "redirect:/login";
        }
    }
} 