package com.next.intune.common.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

// OpenAPI(SecurityScheme) 전역 설정
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name(HttpHeaders.AUTHORIZATION);

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("BearerAuth", bearerScheme))
                // 전역으로 인증 필요하게 적용(필요 시 메서드별로만 걸어도 됨)
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
