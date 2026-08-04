package com.example.mvccrud.member;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MemberResponse {

    @Schema(description = "회원 ID", example = "1")
    private final Long id;

    @Schema(description = "회원 이름", example = "김철수")
    private final String name;

    @Schema(description = "회원 이메일", example = "kim@test.com")
    private final String email;

    @Schema(description = "회원 나이", example = "30")
    private final int age;

    @Schema(description = "생성 시간", example = "2026-07-08T15:12:45.51623")
    private final LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2026-07-08T15:13:30.677527")
    private final LocalDateTime updatedAt;

    private Role role;

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.age = member.getAge();
        this.createdAt = member.getCreatedAt();
        this.updatedAt = member.getUpdatedAt();
        this.role = member.getRole();
    }
}
