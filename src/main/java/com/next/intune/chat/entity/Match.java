package com.next.intune.chat.entity;

import com.next.intune.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "`matches`",
        catalog = "`intune-chat`"
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`match_id`", nullable = false)
    @EqualsAndHashCode.Include
    private Long matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`requester_id`", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`responder_id`", nullable = false)
    private User responder;

    @Builder.Default
    @Column(name = "`is_approved`", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean approved = false;

    @Builder.Default
    @Column(name = "`is_valid`", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean valid = true;

    @Column(name = "`created_at`", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "`updated_at`", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}