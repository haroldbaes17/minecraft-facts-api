package io.github.haroldbaes17.minecraftfacts.dto.RoleDTO;

import io.github.haroldbaes17.minecraftfacts.model.Role;

public record RoleResponse(
        Long id,
        String name,
        String description,
        boolean deleted
) {
    public static RoleResponse from(Role r) {
        return new RoleResponse(r.getId(), r.getName(), r.getDescription(), r.isDeleted());
    }
}
