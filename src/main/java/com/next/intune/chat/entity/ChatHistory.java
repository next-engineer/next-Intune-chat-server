package com.next.intune.chat.entity;

import com.next.intune.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "`chats_history`",
        catalog = "`intune-chat`"
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`chat_id`", nullable = false)
    @EqualsAndHashCode.Include
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "`match_id`", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "`user_id`", nullable = false)
    private User user;

    @Column(name = "`chat`", columnDefinition = "TEXT")
    private String chat;

    @Column(name = "`created_at`", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
