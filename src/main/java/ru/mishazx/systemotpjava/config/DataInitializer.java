package ru.mishazx.systemotpjava.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.mishazx.systemotpjava.services.RoleService;

/**
 * Этот класс инициализирует базовые данные при запуске приложения.
 * Здесь создаются роли пользователей по умолчанию и другие необходимые данные.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;

    @Override
    public void run(String... args) {
        // Создаем роли, если они еще не существуют
        log.info("Инициализация ролей пользователей...");
        roleService.checkRoleOrCreate("USER");
        roleService.checkRoleOrCreate("ADMIN");
        log.info("Инициализация ролей завершена");
    }
} 