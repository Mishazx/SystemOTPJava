package ru.mishazx.systemotpjava.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.mishazx.systemotpjava.models.OTPCode;
import ru.mishazx.systemotpjava.services.OTPService;
import ru.mishazx.systemotpjava.services.NotificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Контроллер для работы с OTP аутентификацией.
 * Управляет выбором метода доставки кода, отправкой и проверкой кода подтверждения.
 */
@Controller
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
@Slf4j
public class OTPAuthController {

    private final OTPService otpService;
    private final NotificationService notificationService;
    
    /**
     * Отображает страницу выбора метода получения OTP кода.
     */
    @GetMapping("/method")
    public String showMethodSelectionPage(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        // Проверяем, есть ли в сессии флаг OTP_VERIFIED
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("OTP_VERIFIED"))) {
            // Если OTP уже проверен, перенаправляем на дашборд
            return "redirect:/dashboard";
        }
        
        model.addAttribute("userId", userId);
        return "auth/otp-method-select";
    }
    
    /**
     * Обрабатывает запрос на отправку OTP кода выбранным методом.
     */
    @PostMapping("/send")
    public String sendOtp(
            @RequestParam("userId") String userId,
            @RequestParam("method") String method,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "telegram", required = false) String telegram,
            RedirectAttributes redirectAttributes) {
        
        try {
            String deliveryAddress;
            String deliveryDestination;
            boolean sent = false;
            
            // Определяем адрес доставки в зависимости от метода
            switch (method) {
                case "SMS":
                    deliveryAddress = phone;
                    deliveryDestination = "телефон " + maskPhone(phone);
                    break;
                case "EMAIL":
                    deliveryAddress = email;
                    deliveryDestination = "email " + maskEmail(email);
                    break;
                case "TELEGRAM":
                    deliveryAddress = telegram;
                    deliveryDestination = "Telegram " + telegram;
                    break;
                default:
                    redirectAttributes.addFlashAttribute("error", "Неизвестный метод доставки");
                    return "redirect:/auth/otp/method";
            }
            
            // Генерируем уникальный ID операции
            String operationId = UUID.randomUUID().toString();
            
            // Генерируем OTP код
            String otpCode = otpService.generateOtp(userId, method, deliveryAddress, operationId);
            log.info("Generated OTP code for user: {}, method: {}, address: {}", userId, method, deliveryAddress);
            
            // Отправляем код соответствующим способом
            switch (method) {
                case "SMS":
                    sent = notificationService.sendSmsOtp(phone, otpCode);
                    break;
                case "EMAIL":
                    sent = notificationService.sendEmailOtp(email, otpCode);
                    break;
                case "TELEGRAM":
                    sent = notificationService.sendTelegramOtp(telegram, otpCode);
                    break;
            }
            
            if (!sent) {
                redirectAttributes.addFlashAttribute("error", "Не удалось отправить код. Пожалуйста, попробуйте другой способ.");
                return "redirect:/auth/otp/method";
            }
            
            // Передаем информацию на страницу проверки кода
            redirectAttributes.addFlashAttribute("userId", userId);
            redirectAttributes.addFlashAttribute("operationId", operationId);
            redirectAttributes.addFlashAttribute("deliveryDestination", deliveryDestination);
            
            return "redirect:/auth/otp/verify";
        } catch (Exception e) {
            log.error("Error sending OTP", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при отправке кода: " + e.getMessage());
            return "redirect:/auth/otp/method";
        }
    }
    
    /**
     * Отображает страницу ввода OTP кода.
     */
    @GetMapping("/verify")
    public String showVerificationPage(Model model, HttpServletRequest request) {
        // Проверяем, есть ли в сессии флаг OTP_VERIFIED
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("OTP_VERIFIED"))) {
            // Если OTP уже проверен, перенаправляем на дашборд
            return "redirect:/dashboard";
        }
        
        // Проверяем, есть ли нужные атрибуты (они должны быть переданы из /send)
        if (!model.containsAttribute("userId") || !model.containsAttribute("operationId")) {
            return "redirect:/auth/otp/method";
        }
        
        return "auth/otp-verify";
    }
    
    /**
     * Проверяет введенный OTP код.
     */
    @PostMapping("/verify")
    public String verifyOtp(
            @RequestParam("userId") String userId,
            @RequestParam("operationId") String operationId,
            @RequestParam("code") String code,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        
        try {
            // Проверяем код
            boolean isValid = otpService.validateOtp(userId, code, operationId);
            
            if (isValid) {
                // Код верный, устанавливаем флаг в сессии и перенаправляем на дашборд
                HttpSession session = request.getSession(true);
                session.setAttribute("OTP_VERIFIED", true);
                log.info("OTP verified successfully for user: {}, set session flag", userId);
                return "redirect:/dashboard";
            } else {
                // Код неверный, возвращаемся на страницу ввода
                log.warn("Invalid OTP code for user: {}", userId);
                redirectAttributes.addFlashAttribute("error", "Неверный код подтверждения");
                redirectAttributes.addFlashAttribute("userId", userId);
                redirectAttributes.addFlashAttribute("operationId", operationId);
                return "redirect:/auth/otp/verify";
            }
        } catch (Exception e) {
            log.error("Error verifying OTP", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при проверке кода: " + e.getMessage());
            return "redirect:/auth/otp/verify";
        }
    }
    
    /**
     * Обрабатывает запрос на повторную отправку кода.
     */
    @GetMapping("/resend")
    public String resendOtp(@RequestParam("userId") String userId, RedirectAttributes redirectAttributes) {
        // Получаем информацию о последнем коде пользователя
        OTPCode lastOtp = otpService.getLastActiveOtp(userId);
        
        if (lastOtp == null) {
            // Если нет активного кода, отправляем на страницу выбора метода
            return "redirect:/auth/otp/method";
        }
        
        // Повторно отправляем код тем же методом
        String method = lastOtp.getDeliveryMethod().name();
        String address = lastOtp.getDeliveryAddress();
        String operationId = UUID.randomUUID().toString();
        
        String deliveryDestination;
        switch (method) {
            case "SMS":
                deliveryDestination = "телефон " + maskPhone(address);
                break;
            case "EMAIL":
                deliveryDestination = "email " + maskEmail(address);
                break;
            case "TELEGRAM":
                deliveryDestination = "Telegram " + address;
                break;
            default:
                deliveryDestination = address;
        }
        
        // Отмечаем старый код как устаревший
        otpService.markAsExpired(lastOtp.getId());
        
        // Генерируем и отправляем новый код
        String otpCode = otpService.generateOtp(userId, method, address, operationId);
        
        // Отправляем код соответствующим способом
        boolean sent = false;
        switch (method) {
            case "SMS":
                sent = notificationService.sendSmsOtp(address, otpCode);
                break;
            case "EMAIL":
                sent = notificationService.sendEmailOtp(address, otpCode);
                break;
            case "TELEGRAM":
                sent = notificationService.sendTelegramOtp(address, otpCode);
                break;
        }
        
        if (!sent) {
            redirectAttributes.addFlashAttribute("error", "Не удалось отправить код. Пожалуйста, попробуйте другой способ.");
            return "redirect:/auth/otp/method";
        }
        
        // Передаем информацию на страницу проверки кода
        redirectAttributes.addFlashAttribute("userId", userId);
        redirectAttributes.addFlashAttribute("operationId", operationId);
        redirectAttributes.addFlashAttribute("deliveryDestination", deliveryDestination);
        
        return "redirect:/auth/otp/verify";
    }
    
    // Вспомогательные методы для маскирования конфиденциальных данных
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) {
            return "***";
        }
        return "***" + phone.substring(phone.length() - 4);
    }
    
    private String maskEmail(String email) {
        if (email == null || email.indexOf('@') == -1) {
            return "***@***";
        }
        
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        
        String maskedName = name.length() <= 2 
            ? "***" 
            : name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
        
        return maskedName + "@" + domain;
    }
} 