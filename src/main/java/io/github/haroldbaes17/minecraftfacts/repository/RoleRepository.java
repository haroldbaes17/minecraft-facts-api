package io.github.haroldbaes17.minecraftfacts.repository;

import io.github.haroldbaes17.minecraftfacts.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Page<Role> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String namePart, String descPart, Pageable pageable);

    boolean existsByName(String name);

    List<Role> findAllByDeleted(boolean deleted);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Role r set r.deleted = true where r.id in :ids and r.deleted = false")
    int softDeleteByIds(@Param("ids") Collection<Long> ids);

}
