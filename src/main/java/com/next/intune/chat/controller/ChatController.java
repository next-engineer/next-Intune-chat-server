package com.next.intune.chat.controller;

import com.next.intune.chat.dto.request.SendChatRequestDto;
import com.next.intune.chat.dto.response.ChatResponseDto;
import com.next.intune.chat.dto.response.MatchResponseDto;
import com.next.intune.chat.entity.Match;
import com.next.intune.chat.service.ChatService;
import com.next.intune.common.api.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Tag(name = "Chat Controller", description = "채팅 API")
@SecurityRequirement(name = "BearerAuth")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/match")
    @Operation(summary = "채팅 매칭")
    public ResponseEntity<ApiResult<?>> match(HttpServletRequest request) {
        chatService.match(request);
        return ResponseEntity.ok(ApiResult.success());
    }

    @GetMapping("/match")
    @Operation(summary = "매칭 목록 불러오기")
    public ResponseEntity<ApiResult<List<MatchResponseDto>>> getMatches(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResult.success(chatService.getMatches(request)));
    }

    @GetMapping("/chat/{matchId}")
    @Operation(summary = "채팅 목록 불러오기")
    public ResponseEntity<ApiResult<List<ChatResponseDto>>> getChats(HttpServletRequest request, @PathVariable("matchId")Long matchId) {
        return ResponseEntity.ok(ApiResult.success(chatService.getChats(request, matchId)));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResult<?>> sendChat(HttpServletRequest request, @RequestBody SendChatRequestDto dto) {
        chatService.sendChat(request, dto);
        return ResponseEntity.ok(ApiResult.success());
    }
}
