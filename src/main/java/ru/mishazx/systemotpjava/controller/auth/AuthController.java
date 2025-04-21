package ru.mishazx.systemotpjava.controller.auth;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mishazx.systemotpjava.models.User;
import ru.mishazx.systemotpjava.models.role.RoleUser;
import ru.mishazx.systemotpjava.repository.UserRepository;
import ru.mishazx.systemotpjava.services.RoleService;

import java.util.Set;

/**
 * Этот контроллер отвечает за вход и регистрацию пользователей.
 * Проще говоря: всё, что связано с авторизацией — обработка форм, создание новых пользователей и т.д.
 * Если пользователь ошибся при входе — выводится сообщение об ошибке.
 * Здесь же происходит сохранение нового пользователя в базу данных.
 */
@Controller
@AllArgsConstructor
public class AuthController {
    // Сервис для работы с ролями пользователей (например, роль "USER")
    private final UserRepository userRepository;
    // Репозиторий для работы с пользователями (сохранение, поиск)
    private final RoleService roleService;
    // Кодировщик паролей (делает пароли безопасными для хранения)
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(RoleService roleService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleService = roleService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Показывает форму входа.
     * Если была ошибка — выводит сообщение об ошибке.
     */
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                HttpSession session, Model model) {
        if (error != null) {
            String errorMessage = (String) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            model.addAttribute("error", errorMessage);
            session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        }
        return "auth/login";
    }

    /**
     * Показывает форму регистрации нового пользователя.
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    /**
     * Обрабатывает отправку формы регистрации.
     * Создаёт нового пользователя, шифрует пароль, назначает роль и сохраняет в базу.
     * После успешной регистрации перенаправляет на страницу входа.
     */
    @PostMapping("/register")
    public String register(@ModelAttribute User user) {
        RoleUser userRole = roleService.findRole("USER");

        User newUser = User.builder()
                .password(passwordEncoder.encode(user.getPassword()))
                .username(user.getUsername())
                .roleUsers(Set.of(userRole))
                .enabled(true)
                .build();

        userRepository.save(newUser);

        return "redirect:/dashboard";
    }
    
    /**
     * Показывает форму восстановления пароля.
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }
    
    /**
     * Обрабатывает запрос на восстановление пароля.
     * В данной реализации просто показывает сообщение об успешной отправке инструкций,
     * но в реальном приложении должен отправлять email со ссылкой для сброса пароля.
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("username") String username, Model model) {
        boolean userExists = userRepository.findByUsername(username).isPresent();
        
        if (userExists) {
            // В реальном приложении здесь должна быть отправка email
            model.addAttribute("message", "Инструкции по восстановлению пароля отправлены на вашу почту.");
        } else {
            model.addAttribute("error", "Пользователь с таким именем не найден.");
        }
        
        return "auth/forgot-password";
    }

}
