package com.next.intune.chat.repository.mapping;

import java.time.Instant;

public interface MatchRow {
    Long getMatchId();
    Long getRequesterId();
    Long getResponderId();
    boolean isApproved();
    boolean isValid();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
