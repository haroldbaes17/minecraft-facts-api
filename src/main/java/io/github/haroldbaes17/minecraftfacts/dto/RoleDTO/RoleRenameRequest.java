package io.github.haroldbaes17.minecraftfacts.dto.RoleDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleRenameRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^ROLE_[A-Z_]+$", message = "Invalid Format. Must be ROLE_ followed by CAPITAL LETTERS. Example: ROLE_EXAMPLE")
        String name
) {
}
