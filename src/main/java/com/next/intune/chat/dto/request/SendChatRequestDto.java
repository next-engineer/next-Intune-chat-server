package com.next.intune.chat.dto.request;

import lombok.Getter;

@Getter
public class SendChatRequestDto {
    private Long matchId;
    private String chat;
}
