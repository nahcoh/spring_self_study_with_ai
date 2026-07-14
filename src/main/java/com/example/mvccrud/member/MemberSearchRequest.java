package com.example.mvccrud.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchRequest {

    @Schema(description = "검색할 회원 이름", example = "김")
    private String name;

    @Schema(description = "검색할 회원 이메일", example = "test.com")
    private String email;

}
