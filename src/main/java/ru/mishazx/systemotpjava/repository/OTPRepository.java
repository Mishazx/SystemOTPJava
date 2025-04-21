package ru.mishazx.systemotpjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.mishazx.systemotpjava.models.OTPCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPCode, Long> {
    // Найти активный код по ID пользователя и коду
    Optional<OTPCode> findByUserIdAndCodeAndStatus(Long userId, String code, OTPCode.OtpStatus status);
    
    // Найти активный код по ID пользователя, коду, статусу и ID операции
    Optional<OTPCode> findByUserIdAndCodeAndStatusAndOperationId(Long userId, String code, OTPCode.OtpStatus status, String operationId);
    
    // Удалить все коды пользователя
    void deleteByUserId(Long userId);

    // Найти все истекшие, но не отмеченные как таковые коды
    List<OTPCode> findByStatusAndExpiresAtLessThan(OTPCode.OtpStatus status, LocalDateTime now);
    
    // Обновить статус для истекших кодов
    @Modifying
    @Transactional
    @Query("UPDATE OTPCode o SET o.status = ru.mishazx.systemotpjava.models.OTPCode.OtpStatus.EXPIRED WHERE o.status = ru.mishazx.systemotpjava.models.OTPCode.OtpStatus.ACTIVE AND o.expiresAt < :now")
    int markExpiredCodes(LocalDateTime now);
}