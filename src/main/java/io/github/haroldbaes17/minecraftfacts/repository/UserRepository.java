package io.github.haroldbaes17.minecraftfacts.repository;

import io.github.haroldbaes17.minecraftfacts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

}
