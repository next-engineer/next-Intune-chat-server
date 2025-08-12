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
        String requesterGender = requester.getGender();
        String oppositeGender = "M".equals(requesterGender) ? "F" : "M";

        // 1) MBTI 호환 점수 로딩
        Map<String, Integer> scoresMap = mbtiCompatibilityLoader.getScoresFor(requesterMbti);

        // 2) 점수별 MBTI 묶기 (동점자 그룹), 이후 점수 내림차순 순회용 키 리스트
        Map<Integer, List<String>> groupedByScore = scoresMap.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));
        List<Integer> scoreOrderDesc = groupedByScore.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        // 3) 각 점수 그룹을 IN 조건으로 조회해서 후보 합치기
        List<User> candidates = new ArrayList<>();
        for (Integer score : scoreOrderDesc) {
            List<String> mbtiGroup = groupedByScore.get(score);
            if (mbtiGroup == null || mbtiGroup.isEmpty()) continue;

            List<User> groupUsers = userRepository
                    .findAllByMbtiInAndGenderAndValidTrue(mbtiGroup, oppositeGender)
                    .stream()
                    .filter(u -> !u.getUserId().equals(requester.getUserId())) // 자기 자신 제외
                    .toList();

            candidates.addAll(groupUsers);
        }

        if (candidates.isEmpty()) {
            throw new CustomException(ResponseCode.RESOURCE_NOT_FOUND);
        }

        // 4) 전체 후보 중 랜덤 1명 선택
        User responder = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));

        // 5) 중복 매칭 방지 (유니크키 + 사전 exists 체크)
        if (!matchRepository.existsByRequesterAndResponder(requester, responder)) {
            Match match = Match.builder()
                    .requester(requester)
                    .responder(responder)
                    .build();
            matchRepository.save(match);
        }
    }
}
