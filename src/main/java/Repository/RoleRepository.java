package Repository;

import models.ERole;
import models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Поиск роли по имени (enum ERole)
    Optional<Role> findByName(ERole name);

    // Проверка существования роли (может пригодиться)
    boolean existsByName(ERole name);
}