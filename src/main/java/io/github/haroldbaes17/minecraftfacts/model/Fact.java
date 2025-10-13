package io.github.haroldbaes17.minecraftfacts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;
import java.util.Set;

@Entity @Table(name = "facts",
        indexes = {
                @Index(name = "idx_facts_title", columnList = "title"),
                @Index(name = "idx_facts_published", columnList = "published")
        })
@Data
public class Fact {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 5, max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank @Size(min = 20, max = 4000)
    @Column(nullable = false, length = 4000)
    private String content;

    // URL devuelta por Cloudinary después del upload
    @NotBlank @URL @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String imageUrl;

    // Opcional: enlace a la fuente / artículo / wiki
    @URL @Size(max = 500)
    private String sourceUrl;

    // Publicado o en borrador
    @Column(nullable = false)
    private boolean published = true;

    // Autor (usuario que creó el dato)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_facts_author"))
    private User author;

    // Muchas categorías por dato curioso
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "fact_categories",
        joinColumns = @JoinColumn(name = "fact_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    // Auditoría
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    // Soft-delete
    @Column(nullable = false)
    private boolean deleted = false;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
