package com.example.mvccrud.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {

    @Schema(description = "수정할 회원 이름", example = "수정된 이름")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "수정할 회원 이메일", example = "new@test.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "수정할 회원 나이", example = "35")
    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    private int age;

    public MemberUpdateRequest(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
}
