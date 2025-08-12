package com.next.intune.chat.service;

import com.next.intune.chat.component.MbtiCompatibilityLoader;
import com.next.intune.chat.entity.Match;
import com.next.intune.chat.repository.ChatHistoryRepository;
import com.next.intune.chat.repository.ChatImageRepository;
import com.next.intune.chat.repository.MatchRepository;
import com.next.intune.common.api.CustomException;
import com.next.intune.common.api.ResponseCode;
import com.next.intune.common.security.jwt.JwtProvider;
import com.next.intune.user.entity.User;
import com.next.intune.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final JwtProvider jwtProvider;
    private final MbtiCompatibilityLoader mbtiCompatibilityLoader;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatImageRepository chatImageRepository;

    @Transactional
    public void match(HttpServletRequest request) {
        String email = jwtProvider.extractEmailFromRequest(request);
        User requester = userRepository.findByEmailAndValidTrue(email)
                .orElseThrow(() -> new CustomException(ResponseCode.UNAUTHORIZED));

        String requesterMbti = requester.getMbti();
        String oppositeGender = "M".equals(requester.getGender()) ? "F" : "M";

        // 1) MBTI 점수 맵 불러오기
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

        // 3) 최고점 그룹부터 순회: 그룹 내에서만 랜덤 1명 뽑기, 성공하면 바로 종료
        for (Integer score : scoreOrderDesc) {
            List<String> mbtiGroup = groupedByScore.get(score);
            if (mbtiGroup == null || mbtiGroup.isEmpty()) continue;

            // (권장) MySQL 네이티브로 그룹 내 랜덤 1명
            Optional<User> responderOpt =
                    userRepository.pickOneRandomByMbtiInAndGenderAndValidTrue(
                            mbtiGroup, oppositeGender, requester.getUserId());

            if (responderOpt.isEmpty()) {
                // 이 점수대에 후보가 없으면 다음 점수대로
                continue;
            }

            User responder = responderOpt.get();

            // 동시성 안전 가드: 유니크 제약에 걸리면 스킵/재시도
            try {
                if (!matchRepository.existsByRequesterAndResponderAndValidTrue(requester, responder)) {
                    matchRepository.save(Match.builder()
                            .requester(requester)
                            .responder(responder)
                            .build());
                }
                return; // 매칭 성공했으니 즉시 종료
            } catch (DataIntegrityViolationException e) {
                // 같은 타이밍에 다른 트랜잭션이 선점 저장한 경우로 보고 다음 점수대로 진행

            }
        }

        // 모든 점수대에서 후보를 못 찾음
        throw new CustomException(ResponseCode.RESOURCE_NOT_FOUND);
    }
}
