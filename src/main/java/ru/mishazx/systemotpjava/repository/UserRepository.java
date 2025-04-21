package ru.mishazx.systemotpjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mishazx.systemotpjava.models.User;

import java.util.List;
import java.util.Optional;


/**
 * Репозиторий для работы с пользователями.
 * Здесь описаны методы поиска и сохранения пользователей в базе данных.
 * Если нужно найти пользователя по логину — использовать findByUsername()
 */
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    /**
     * Ищет пользователя по логину (username).
     * @param login логин пользователя
     * @return Optional<User> — либо пользователь, либо пусто
     */
    Optional<User> findByUsername (String login);


    /**
     * Ищет всех пользователей, кроме тех, у которых роль ADMIN.
     * @param roleName имя роли
     * @return список пользователей
     */
    List<User> findByRoleUsersNameRoleNot(String roleName);
}
