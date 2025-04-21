package ru.mishazx.systemotpjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mishazx.systemotpjava.models.OTPConfig;

import java.util.Optional;

@Repository
public interface OTPConfigRepository extends JpaRepository<OTPConfig, Long> {
    // Получение единственной конфигурации
    Optional<OTPConfig> findFirstBy();
}