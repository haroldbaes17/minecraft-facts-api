package io.github.haroldbaes17.minecraftfacts.dto.RoleDTO;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkRestoreRolesRequest(
        @NotEmpty(message = "ids no puede estar vacío")
        List<Long> ids
) {
}
