package com.example.mvccrud.member;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberPatchRequest {

    @Schema(description = "부분 수정할 회원 이름", example = "이름만 수정")
    private String name;

    @Schema(description = "부분 수정할 회원 이메일", example = "patch@test.com")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "부분 수정할 회원 나이", example = "40")
    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    private Integer age;

    public MemberPatchRequest(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
}
