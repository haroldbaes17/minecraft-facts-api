package io.github.haroldbaes17.minecraftfacts.dto.RoleDTO;

import java.util.List;

public record BulkRestoreRolesResponse(
        int requested,
        int restored,
        List<BulkRestoreRestored> restoredRoles,
        List<BulkRestoreSkipped> skipped
) {}
