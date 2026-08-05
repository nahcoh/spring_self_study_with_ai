package com.example.mvccrud.global;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Spring MVC CRUD Practice API")
                .description(
                    "Book, Member, Order 도메인을 기반으로 CRUD, 검색, 페이징, JPA 연관관계, 예외 처리, 테스트를 연습한 REST API 문서입니다.")
                .version("v1.0.0")
            )
            .addSecurityItem(new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME)
            )
            .components(new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            );
    }


}
