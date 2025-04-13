package Repository;

import models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    // Проверка существования пользователя по имени
    boolean existsByUsername(String username);

    // Проверка существования пользователя по email
    boolean existsByEmail(String email);

    // Поиск пользователя по имени пользователя
    Optional<Users> findByUsername(String username);

    // Поиск пользователя по email (может пригодиться для восстановления пароля)
    Optional<Users> findByEmail(String email);

}