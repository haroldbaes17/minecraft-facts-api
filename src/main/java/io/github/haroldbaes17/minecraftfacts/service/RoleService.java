package io.github.haroldbaes17.minecraftfacts.service;

import io.github.haroldbaes17.minecraftfacts.dto.*;
import io.github.haroldbaes17.minecraftfacts.dto.RoleDTO.*;
import io.github.haroldbaes17.minecraftfacts.exception.DuplicateResourceException;
import io.github.haroldbaes17.minecraftfacts.exception.ResourceNotFoundException;
import io.github.haroldbaes17.minecraftfacts.exception.RoleInUseException;
import io.github.haroldbaes17.minecraftfacts.exception.RoleNotDeletedException;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /* ========= Lectura / búsqueda ========= */
    public List<RoleResponse> findAll() {
        return roleRepository.findAll()
                .stream()
                .map(RoleResponse::from)
                .toList();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }

    public RoleResponse findByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        return RoleResponse.from(role);
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

    @Transactional
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
    @Transactional
    public String delete(long id) {
        Role role = findById(id);

        boolean hasUsers = userRepository.existsByRoles_Id(role.getId());
        if (hasUsers) {
            throw new RoleInUseException("Role in use. You cannot delete it.");
        }

        role.setDeleted(true);
        roleRepository.save(role);

        return "Role has been deleted";
    }

    public List<RoleResponse> listTrash() {
        return roleRepository.findAllByDeleted(true)
                .stream()
                .map(RoleResponse::from)
                .toList();
    }

    public RoleResponse restore(long id) {
        Role role = findById(id);

        if (!role.isDeleted()) {
            return RoleResponse.from(role);
        }

        role.setDeleted(false);
        roleRepository.save(role);
        return RoleResponse.from(role);
    }

    @Transactional
    public BulkDeleteRolesResponse bulkDelete(BulkDeleteRolesRequest req) {
        List<Long> input = Optional.ofNullable(req.ids()).orElse(List.of());
        LinkedHashSet<Long> uniqueIds = input.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (uniqueIds.isEmpty()) {
            return new BulkDeleteRolesResponse(0, 0, List.of(), List.of());
        }

        Map<Long, Role> rolesById = new HashMap<>();
        roleRepository.findAllById(uniqueIds).forEach(r -> rolesById.put(r.getId(), r));

        List<BulkDeleteSkipped> skipped = new ArrayList<>();

        Set<Long> existingIds = rolesById.keySet();

        for (Long id : uniqueIds) {
            if (!existingIds.contains(id)) {
                skipped.add(new BulkDeleteSkipped(id, null, "Role not found"));
            }
        }

        List<Role> alreadyDeleted = rolesById.values().stream()
                .filter(Role::isDeleted)
                .toList();
        alreadyDeleted.forEach(r ->
                skipped.add(new BulkDeleteSkipped(r.getId(), r.getName(), "Rol already deleted"))
        );

        List<Role> activeRoles = rolesById.values().stream()
                .filter(r -> !r.isDeleted())
                .toList();

        Set<Long> inUseIds = userRepository.findRoleIdsInUse(
                activeRoles.stream().map(Role::getId).toList()
        );
        inUseIds.forEach(id -> {
            Role r = rolesById.get(id);
            if (r != null)
                skipped.add(new BulkDeleteSkipped(r.getId(), r.getName(), "Role in use"));
        });

        List<Role> toDelete = activeRoles.stream()
                .filter(r -> !inUseIds.contains(r.getId()))
                .toList();

        List<BulkDeleteDeleted> deletedRoles = new ArrayList<>();
        if (!toDelete.isEmpty()) {
            toDelete.forEach(r -> r.setDeleted(true));
            roleRepository.saveAll(toDelete);

            deletedRoles = toDelete.stream()
                    .map(r -> new BulkDeleteDeleted(r.getId(), r.getName()))
                    .toList();
        }

        return new BulkDeleteRolesResponse(
                input.size(),
                deletedRoles.size(),
                deletedRoles,
                skipped
        );
    }

    @Transactional
    public BulkRestoreRolesResponse bulkRestore(BulkRestoreRolesRequest req) {
        List<Long> input = Optional.ofNullable(req.ids()).orElse(List.of());

        LinkedHashSet<Long> uniqueIds = input.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (uniqueIds.isEmpty()) {
            return new BulkRestoreRolesResponse(0, 0, List.of(), List.of());
        }

        Map<Long, Role> rolesById = new HashMap<>();
        roleRepository.findAllById(uniqueIds).forEach(r -> rolesById.put(r.getId(), r));

        List<BulkRestoreSkipped> skipped = new ArrayList<>();

        for (Long id : uniqueIds) {
            if (!rolesById.containsKey(id)) {
                skipped.add(new BulkRestoreSkipped(id, null, "Role not found"));
            }
        }

        List<Role> alreadyActive = new ArrayList<>();
        List<Role> toRestore = new ArrayList<>();

        for (Role r : rolesById.values()) {
            if (r.isDeleted()) {
                toRestore.add(r);
            } else {
                alreadyActive.add(r);
            }
        }

        alreadyActive.forEach(r ->
                skipped.add(new BulkRestoreSkipped(r.getId(), r.getName(), "Role not deleted"))
        );

        List<BulkRestoreRestored> restoredRoles = new ArrayList<>();
        if (!toRestore.isEmpty()) {
            toRestore.forEach(r -> r.setDeleted(false));
            roleRepository.saveAll(toRestore);
            restoredRoles = toRestore.stream()
                    .map(r -> new BulkRestoreRestored(r.getId(), r.getName()))
                    .toList();
        }

        return new BulkRestoreRolesResponse(
                input.size(),
                restoredRoles.size(),
                restoredRoles,
                skipped
        );
    }

    @Transactional
    public String hardDelete(Long id) {
        Role role = findById(id);

        if (!role.isDeleted()) {
            throw new RoleNotDeletedException("Role is not deleted.");
        }

        roleRepository.deleteById(id);
        return "Role has been deleted";
    }
}
