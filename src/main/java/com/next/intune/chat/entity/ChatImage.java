package com.next.intune.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "`chats_images`",
        catalog = "`intune-chat`"
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`chat_image_id`", nullable = false)
    @EqualsAndHashCode.Include
    private Long chatImageId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "`chat_id`", nullable = false)
    private ChatHistory chatHistory;

    @Column(name = "`chat_image_name`", nullable = false, length = 255)
    private String chatImageName;

    @Column(name = "`created_at`", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
