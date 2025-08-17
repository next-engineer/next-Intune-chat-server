package com.next.intune.chat.component;

import com.next.intune.common.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            // STOMP CONNECT 프레임의 헤더에서 Authorization 추출
            String auth = acc.getFirstNativeHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing Authorization header");
            }
            String token = auth.substring(7);
            if (!jwtProvider.validateAccessToken(token)) {
                throw new IllegalArgumentException("Invalid token");
            }

            String email = jwtProvider.getEmailFromAccessToken(token);

            // 이후 @MessageMapping 등에서 Principal 사용 가능
            acc.setUser(new UsernamePasswordAuthenticationToken(email, null, List.of()));
        }
        return message;
    }
}
