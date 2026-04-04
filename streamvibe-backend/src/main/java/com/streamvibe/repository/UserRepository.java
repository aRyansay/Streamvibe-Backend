// ─── UserRepository.java ────────────────────────────────────────
package com.streamvibe.repository;

import com.streamvibe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findAllByIdIn(@Param("ids") List<Long> ids);
}
