package com.next.intune.chat.socket;

import com.next.intune.chat.component.JwtStompChannelInterceptor;
import com.next.intune.common.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtProvider jwtProvider;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 핸드셰이크용 엔드포인트 (쿼리파라미터 사용 안함)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // 배포 시 프론트 도메인으로 제한 권장
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 서버 -> 클라 브로드캐스트 목적지 프리픽스
        registry.enableSimpleBroker("/topic", "/queue");
        // 클라 -> 서버 전송 프리픽스
        registry.setApplicationDestinationPrefixes("/app");
        // 사용자 개별 목적지 프리픽스 (선택)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // CONNECT 프레임에서 Authorization 헤더 검증
        registration.interceptors(new JwtStompChannelInterceptor(jwtProvider));
    }
}
