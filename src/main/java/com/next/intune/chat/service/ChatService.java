package com.next.intune.chat.service;

import com.next.intune.chat.component.MbtiCompatibilityLoader;
import com.next.intune.chat.dto.request.SendChatRequestDto;
import com.next.intune.chat.dto.response.ChatResponseDto;
import com.next.intune.chat.dto.response.MatchResponseDto;
import com.next.intune.chat.entity.ChatHistory;
import com.next.intune.chat.entity.Match;
import com.next.intune.chat.repository.ChatHistoryRepository;
import com.next.intune.chat.repository.MatchRepository;
import com.next.intune.common.api.CustomException;
import com.next.intune.common.api.ResponseCode;
import com.next.intune.common.helper.MySqlAdvisoryLock;
import com.next.intune.common.security.jwt.JwtProvider;
import com.next.intune.user.entity.User;
import com.next.intune.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final JwtProvider jwtProvider;
    private final MySqlAdvisoryLock advisoryLock;
    private final MbtiCompatibilityLoader mbtiCompatibilityLoader;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    @Transactional
    public void match(HttpServletRequest request) {
        // 0) 요청자 식별
        String email = jwtProvider.extractEmailFromRequest(request);
        User requester = userRepository.findByEmailAndValidTrue(email)
                .orElseThrow(() -> new CustomException(ResponseCode.UNAUTHORIZED));

        String requesterMbti = requester.getMbti();
        String oppositeGender = "M".equals(requester.getGender()) ? "F" : "M";

        // 1) MBTI 호환 점수 로딩
        Map<String, Integer> scoresMap = mbtiCompatibilityLoader.getScoresFor(requesterMbti);

        // 2) 점수 → MBTI 그룹화 & 점수 내림차순 키 리스트
        Map<Integer, List<String>> groupedByScore = scoresMap.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));
        List<Integer> scoreOrderDesc = groupedByScore.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        // 3) 최고점 그룹부터 순회: 그룹 내에서만 "과거 미매칭" 랜덤 1명 시도
        for (Integer score : scoreOrderDesc) {
            List<String> mbtiGroup = groupedByScore.get(score);
            if (mbtiGroup == null || mbtiGroup.isEmpty()) continue;

            // 서브쿼리 방식: 이미 requester와 매칭된 responder 제외하고 랜덤 1명 ID만 조회
            Optional<Long> responderIdOpt = userRepository.pickRandomFreshUserId(
                    requester.getUserId(), mbtiGroup, oppositeGender, requester.getUserId());

            // 이 점수대에 "새로운" 후보가 없으면 다음 점수대로
            if (responderIdOpt.isEmpty()) continue;

            // ID로 엔티티 로드 (엔티티 변경에 강함 / 컬럼 나열 불필요)
            User responder = userRepository.findById(responderIdOpt.get())
                    .orElse(null);
            // 드물게 사이드 이펙트로 사라졌다면 다음 그룹으로
            if (responder == null) continue;

            long a = Math.min(requester.getUserId(), responder.getUserId());
            long b = Math.max(requester.getUserId(), responder.getUserId());
            String lockKey = "match:%d:%d".formatted(a, b);

            boolean created = advisoryLock.withLock(lockKey, Duration.ofSeconds(2), () -> {
                if (!matchRepository.existsAnyDirection(a, b)) {
                    matchRepository.save(Match.builder()
                            .requester(requester)
                            .responder(responder)
                            .build());
                    return true;
                }
                return false;
            });

            if (created) return; // 성공 시에만 종료
        }

        // 모든 점수대에서 후보를 못 찾음
        throw new CustomException(ResponseCode.RESOURCE_NOT_FOUND);
    }

    public List<MatchResponseDto> getMatches(HttpServletRequest request) {
        String email = jwtProvider.extractEmailFromRequest(request);
        User user = userRepository.findByEmailAndValidTrue(email)
                .orElseThrow(() -> new CustomException(ResponseCode.UNAUTHORIZED));
        return matchRepository.findAllValidRowsByUser(user.getUserId()).stream()
                .map(r -> MatchResponseDto.builder()
                        .matchId(r.getMatchId())
                        .name(r.getName())
                        .lastMessage(r.getLastMessage())
                        .approved(r.isApproved())
                        .valid(r.isValid())
                        .createdAt(r.getCreatedAt())
                        .updatedAt(r.getUpdatedAt())
                        .build())
                .toList();
    }

    public List<ChatResponseDto> getChats(HttpServletRequest request, Long matchId) {
        String token = jwtProvider.extractAccessTokenFromHeader(request);
        if (!StringUtils.hasText(token) || !jwtProvider.validateAccessToken(token)) {
            throw new CustomException(ResponseCode.UNAUTHORIZED);
        }
        return chatHistoryRepository.findAllRowsByChat(matchId).stream()
                .map(r -> ChatResponseDto.builder()
                        .chatId(r.getChatId())
                        .matchId(r.getMatchId())
                        .userId(r.getUserId())
                        .chat(r.getChat())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void sendChat(HttpServletRequest request, SendChatRequestDto dto) {
        String email = jwtProvider.extractEmailFromRequest(request);
        User user = userRepository.findByEmailAndValidTrue(email)
                .orElseThrow(() -> new CustomException(ResponseCode.UNAUTHORIZED));

        Match matchRef = Match.builder()
                .matchId(dto.getMatchId())
                .build();

        ChatHistory saved = ChatHistory.builder()
                .match(matchRef)
                .user(user)
                .chat(dto.getChat())
                .build();
        chatHistoryRepository.save(saved);

        ChatResponseDto payload = ChatResponseDto.builder()
                .chatId(saved.getChatId())
                .matchId(saved.getMatch().getMatchId())
                .userId(user.getUserId())
                .chat(saved.getChat())
                .createdAt(saved.getCreatedAt())
                .build();
        messagingTemplate.convertAndSend("/topic/match/" + dto.getMatchId(), payload);
    }
}
