package com.next.intune.chat.controller;

import com.next.intune.chat.service.ChatService;
import com.next.intune.common.api.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/match")
    public ResponseEntity<ApiResult<?>> match(HttpServletRequest request) {
        chatService.match(request);
        return ResponseEntity.ok(ApiResult.success());
    }
}
