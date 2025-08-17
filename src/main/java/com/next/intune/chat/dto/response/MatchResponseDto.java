package com.next.intune.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class MatchResponseDto {
    private Long matchId;
    private String name;
    private String lastMessage;
    private boolean approved;
    private boolean valid;
    private Instant createdAt;
    private Instant updatedAt;
}