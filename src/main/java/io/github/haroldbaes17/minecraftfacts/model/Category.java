package io.github.haroldbaes17.minecraftfacts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_categories_slug", columnNames = "slug"))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 2, max = 60)
    @Column(nullable = false, length = 60)
    private String name;

    //Slug unico para URls limpias (ej. "mobs", "historia")
    @NotBlank @Size(min = 2, max = 64)
    @Column(nullable = false, length = 64)
    private String slug;

    @Size(max = 200)
    private String description;
}
