package io.github.haroldbaes17.minecraftfacts.repository;

import io.github.haroldbaes17.minecraftfacts.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Page<User> findDistinctByRoles_Id(Long roleId, Pageable pageable);

    boolean existsByRoles_Id(Long roleId);

    @Query("select distinct r.id from User u join u.roles r where r.id in :roleIds")
    Set<Long> findRoleIdsInUse(@Param("roleIds") Collection<Long> roleIds);
}
