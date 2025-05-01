package com.example.javas.repository;

import com.example.javas.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);

    @Modifying
    @Query(value = "INSERT INTO user_roles (user_id, role_id) " +
            "SELECT :userId, r.id FROM roles r WHERE r.name = :roleName", 
            nativeQuery = true)
    void assignRoleToUser(@Param("userId") Long userId, @Param("roleName") String roleName);
} 