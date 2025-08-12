package com.next.intune.chat.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.next.intune.common.api.CustomException;
import com.next.intune.common.api.ResponseCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MbtiCompatibilityLoader {
    private final ObjectMapper objectMapper;
    private Map<String, Map<String, Integer>> compatibility;

    @PostConstruct
    private void load() {
        try (InputStream is = new ClassPathResource("data/mbti_compatibility.json").getInputStream()) {
            TypeReference<Map<String, Map<String, Integer>>> typeRef =
                    new TypeReference<>() {};
            this.compatibility = objectMapper.readValue(is, typeRef);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load mbti_compatibility.json", e);
        }
    }

    public Map<String, Integer> getScoresFor(String mbti) {
        Map<String, Integer> scores = compatibility.get(mbti);
        if (scores == null || scores.isEmpty()) {
            throw new CustomException(ResponseCode.RESOURCE_NOT_FOUND);
        }
        return scores;
    }
}
