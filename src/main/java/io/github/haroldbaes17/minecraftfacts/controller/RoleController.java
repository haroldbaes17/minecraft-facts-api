package io.github.haroldbaes17.minecraftfacts.controller;

import io.github.haroldbaes17.minecraftfacts.dto.*;
import io.github.haroldbaes17.minecraftfacts.dto.RoleDTO.*;
import io.github.haroldbaes17.minecraftfacts.model.Role;
import io.github.haroldbaes17.minecraftfacts.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /* ========= Lectura / búsqueda ========= */
    @GetMapping("/findAll")
    public ResponseEntity<List<RoleResponse>> findAll() {
        List<RoleResponse> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<RoleResponse> findById(@PathVariable Long id) {
        Role role = roleService.findById(id);
        return ResponseEntity.ok(RoleResponse.from(role));
    }

    @GetMapping("/findByName/{name}")
    public ResponseEntity<RoleResponse> findByName(@PathVariable String name) {
        RoleResponse response = roleService.findByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Role>> search(
            @RequestParam(defaultValue = "") String q,
            @ParameterObject Pageable pageable) {

        Page<Role> roles = roleService.search(q, pageable);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        Long count = roleService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/existsByName/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable String name) {
        Boolean exists = roleService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/listUsersByRole/{id}")
    public ResponseEntity<Page<UserSummaryDTO>> listUsersByRole(
            @PathVariable Long id,
            @ParameterObject
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(roleService.listUsersByRole(id, pageable));
    }

    /* ========= Creación / actualización ========= */
    @PostMapping("/create")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse created = roleService.create(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        RoleResponse updated = roleService.update(id,request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/rename/{id}")
    public ResponseEntity<RoleResponse> rename(@PathVariable Long id, @Valid @RequestBody RoleRenameRequest request) {
        RoleResponse updated = roleService.rename(id,request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/updateDescription/{id}")
    public ResponseEntity<RoleResponse> updateDescription(@PathVariable Long id, @Valid @RequestBody RoleUpdateDescriptionRequest request) {
        RoleResponse updated = roleService.updateDescription(id, request);
        return ResponseEntity.ok(updated);
    }

    /* ========= Eliminación / recuperación ========= */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok("Role has been deleted.");
    }

    @GetMapping("/listTrash")
    public ResponseEntity<List<RoleResponse>> listTrash() {
        List<RoleResponse> roles = roleService.listTrash();
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/restore/{id}")
    public  ResponseEntity<RoleResponse> restore(@PathVariable Long id) {
        RoleResponse updated = roleService.restore(id);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/bulk/delete")
    public ResponseEntity<BulkDeleteRolesResponse> bulkDelete(
            @Valid @RequestBody BulkDeleteRolesRequest request) {
        BulkDeleteRolesResponse response = roleService.bulkDelete(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/bulk/restore")
    public ResponseEntity<BulkRestoreRolesResponse> bulkRestore(
            @Valid @RequestBody BulkRestoreRolesRequest request) {
        BulkRestoreRolesResponse response = roleService.bulkRestore(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/hardDelete/{id}")
    public ResponseEntity<String> hardDelete(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.hardDelete(id));
    }
}
