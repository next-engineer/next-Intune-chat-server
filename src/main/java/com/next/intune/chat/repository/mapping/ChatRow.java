package com.next.intune.chat.repository.mapping;

import java.time.Instant;

public interface ChatRow {
    Long getChatId();
    Long getMatchId();
    Long getUserId();
    String getChat();
    Instant getCreatedAt();
}
