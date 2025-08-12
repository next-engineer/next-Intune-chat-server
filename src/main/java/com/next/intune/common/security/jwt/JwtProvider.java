package com.next.intune.common.security.jwt;

import com.next.intune.common.api.CustomException;
import com.next.intune.common.api.ResponseCode;
import com.next.intune.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {
    @Getter
    private final Key accessKey;
    private final long accessTokenExpireTime;

    public JwtProvider(
            @Value("${jwt.access.secret}") String accessSecretKey,
            @Value("${jwt.access.expiration}") long accessTokenExpireTime
    ) {
        byte[] accessKeyBytes = Decoders.BASE64.decode(accessSecretKey);
        this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);
        this.accessTokenExpireTime = accessTokenExpireTime;
    }

    // 1. JWT 생성
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpireTime);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("authority", user.getAuthority())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. JWT 검증
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(accessKey)
                    .build()
                    .parseClaimsJws(token); // 여기서 오류나면 예외 발생
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 3. 정보추출
    public Claims getAccessClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromAccessToken(String token) {
        return getAccessClaims(token).getSubject();
    }

    public String getAuthorityFromAccessToken(String token) {
        return getAccessClaims(token).get("authority", String.class);
    }

    public long getExpirationFromAccessToken(String token) {
        return getAccessClaims(token).getExpiration().getTime();
    }

    // 4. Authorization 헤더에서 Bearer 토큰 추출
    public String extractAccessTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // "Bearer " 이후의 토큰 부분만 잘라냄
        }
        return null;
    }

    // 5. 요청으로 부터 추출
    public String extractEmailFromRequest(HttpServletRequest request) {
        String token = extractAccessTokenFromHeader(request);
        if (!StringUtils.hasText(token) || !validateAccessToken(token)) {
            throw new CustomException(ResponseCode.UNAUTHORIZED);
        }
        return getEmailFromAccessToken(token);
    }


}
