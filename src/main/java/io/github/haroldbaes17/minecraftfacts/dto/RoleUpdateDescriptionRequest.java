package io.github.haroldbaes17.minecraftfacts.dto;

import jakarta.validation.constraints.Size;

public record RoleUpdateDescriptionRequest(
        @Size(max = 200)
        String description
) {
}
