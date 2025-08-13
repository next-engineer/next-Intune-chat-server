package com.next.intune.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class ChatResponseDto {
    private Long chatId;
    private Long matchId;
    private Long userId;
    private String chat;
    private Instant createdAt;
}
