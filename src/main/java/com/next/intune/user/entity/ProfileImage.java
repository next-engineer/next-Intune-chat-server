package com.next.intune.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "`profile_images`")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`profile_image_id`", nullable = false)
    @EqualsAndHashCode.Include
    private Long profileImageId;

    @Column(name = "`profile_image_name`", nullable = false, length = 255)
    private String profileImageName;

    @Column(name = "`created_at`", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

