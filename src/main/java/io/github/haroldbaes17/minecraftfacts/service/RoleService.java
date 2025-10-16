package io.github.haroldbaes17.minecraftfacts.service;

import io.github.haroldbaes17.minecraftfacts.dto.*;
import io.github.haroldbaes17.minecraftfacts.exception.DuplicateResourceException;
import io.github.haroldbaes17.minecraftfacts.exception.ResourceNotFoundException;
import io.github.haroldbaes17.minecraftfacts.model.Role;
import io.github.haroldbaes17.minecraftfacts.repository.RoleRepository;
import io.github.haroldbaes17.minecraftfacts.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /* ========= Lectura / búsqueda ========= */
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
    }

    public Page<Role> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) return roleRepository.findAll(pageable);
        return roleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q, pageable);
    }

    public Long count() {
        return roleRepository.count();
    }

    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    public Page<UserSummaryDTO> listUsersByRole(Long roleId, Pageable pageable) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        return userRepository.findDistinctByRoles_Id(role.getId(), pageable)
                .map(UserSummaryDTO::fromEntity);
    }

    /* ========= Creación / actualización ========= */
    @Transactional
    public RoleResponse create(RoleRequest req) {
        String normalized = req.name().trim().toUpperCase(Locale.ROOT);

        if (!normalized.matches("^ROLE_[A-Z_]+$")) throw new ConstraintViolationException("Invalid role name", Set.of());

        if (roleRepository.existsByName(req.name())) throw new DuplicateResourceException("Role already exists");

        Role toSave = Role.builder()
                .name(normalized)
                .description(req.description())
                .build();
        Role saved = roleRepository.save(toSave);
        return RoleResponse.from(saved);
    }

    @Transactional
    public RoleResponse update(Long id, RoleRequest req) {
        Role role = findById(id);

        String normalized = req.name().trim().toUpperCase(Locale.ROOT);

        if (!normalized.matches("^ROLE_[A-Z_]+$")) throw new ConstraintViolationException("Invalid role name", Set.of());

        role.setName(normalized);
        role.setDescription(req.description());
        roleRepository.save(role);
        return RoleResponse.from(role);
    }

    @Transactional
    public RoleResponse rename(long id, RoleRenameRequest request) {
        Role role = findById(id);

        String normalized = request.name().trim().toUpperCase(Locale.ROOT);

        if (!normalized.matches("^ROLE_[A-Z_]+$")) throw new IllegalArgumentException("Invalid role name");

        if (normalized.equals(role.getName())) {
            return RoleResponse.from(role);
        }

        if (roleRepository.existsByName(normalized)) {
            throw new DuplicateResourceException("Role already exists");
        }

        role.setName(normalized);
        roleRepository.save(role);
        return RoleResponse.from(role);
    }

    public RoleResponse updateDescription(long id, RoleUpdateDescriptionRequest request) {
        Role role = findById(id);

        if (role.getDescription().equals(request.description())) {
            return RoleResponse.from(role);
        }

        role.setDescription(request.description());
        roleRepository.save(role);
        return RoleResponse.from(role);
    }

    /* ========= Eliminación / recuperación ========= */

}
