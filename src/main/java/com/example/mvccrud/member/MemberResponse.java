package com.example.mvccrud.member;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MemberResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final int age;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.age = member.getAge();
        this.createdAt = member.getCreatedAt();
        this.updatedAt = member.getUpdatedAt();

    }
}
