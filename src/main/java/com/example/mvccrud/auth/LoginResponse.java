package com.example.mvccrud.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(

    @Schema(description = "회원 ID", example = "1")
    Long memberId,

    @Schema(description = "이메일", example = "kim@test.com")
    String email,

    @Schema(description = "메시지", example = "로그인 성공")
    String message) {


}
