package com.next.intune.chat.controller;

import com.next.intune.chat.service.ChatService;
import com.next.intune.common.api.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Tag(name = "Chat Controller", description = "채팅 API")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/match")
    @Operation(summary = "채팅 매칭", description = "이메일과 비밀번호을 입력해주세요.")
    public ResponseEntity<ApiResult<?>> match(HttpServletRequest request) {
        chatService.match(request);
        return ResponseEntity.ok(ApiResult.success());
    }
}
