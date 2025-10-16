package io.github.haroldbaes17.minecraftfacts.repository;

import io.github.haroldbaes17.minecraftfacts.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
