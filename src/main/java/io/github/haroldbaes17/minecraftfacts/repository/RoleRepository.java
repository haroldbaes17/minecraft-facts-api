package io.github.haroldbaes17.minecraftfacts.repository;

import io.github.haroldbaes17.minecraftfacts.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Page<Role> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String namePart, String descPart, Pageable pageable);

    boolean existsByName(String name);
}
