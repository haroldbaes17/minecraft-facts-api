package io.github.haroldbaes17.minecraftfacts.dto.RoleDTO;

import java.util.List;

public record BulkDeleteRolesResponse(
        int requested,
        int deleted,
        List<BulkDeleteDeleted> deletedRoles,
        List<BulkDeleteSkipped> skipped
) {}