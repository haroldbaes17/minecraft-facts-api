package io.github.haroldbaes17.minecraftfacts.dto;

import io.github.haroldbaes17.minecraftfacts.model.Role;

public record RoleResponse(
        Long id,
        String name,
        String description
) {
    public static RoleResponse from(Role r) {
        return new RoleResponse(r.getId(), r.getName(), r.getDescription());
    }
}
