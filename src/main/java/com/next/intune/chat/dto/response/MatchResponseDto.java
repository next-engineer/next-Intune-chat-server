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
    private Long requesterId;
    private Long responderId;
    private boolean approved;
    private boolean valid;
    private Instant createdAt;
    private Instant updatedAt;
}