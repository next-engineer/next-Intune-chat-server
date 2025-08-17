package com.next.intune.chat.repository.mapping;

import java.time.Instant;

public interface MatchRow {
    Long getMatchId();
    String getName();
    String getLastMessage();
    boolean isApproved();
    boolean isValid();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
