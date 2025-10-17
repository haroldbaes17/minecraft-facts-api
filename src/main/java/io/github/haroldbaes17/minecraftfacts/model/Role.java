package io.github.haroldbaes17.minecraftfacts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity @Table(name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Pattern(regexp = "ROLE_[A-Z_]+")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @Size(max = 200)
    private String description;

    @Column(nullable = false)
    private boolean deleted = false;
}
