package io.github.haroldbaes17.minecraftfacts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Entity @Table(name = "refresh_tokens",
        indexes = {
            @Index(name = "idx_refresh_user", columnList = "user_id"),
            @Index(name = "idx_refresh_expires", columnList = "expiresAt")
        })
@Data
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 36, max = 512)
    @Column(nullable = false, length = 512, unique = true)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_refresh_user"))
    private User user;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;
}
