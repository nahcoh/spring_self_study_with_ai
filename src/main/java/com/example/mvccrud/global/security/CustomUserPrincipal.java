package com.example.mvccrud.global.security;

public record CustomUserPrincipal(
    Long memberId,
    String email,
    String role
) {
}
