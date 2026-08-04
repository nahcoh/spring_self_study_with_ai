package com.example.mvccrud.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.mvccrud.global.security.JwtProvider;
import com.example.mvccrud.member.MemberService;
import com.example.mvccrud.member.MemoryMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthServiceTest {

    private AuthService authService;
    private MemberService memberService;
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        MemoryMemberRepository memberRepository = new MemoryMemberRepository();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        jwtProvider = new JwtProvider(
            "this-is-a-very-long-secret-key-for-jwt-token-signing-practice-2026",
            3600L
        );

        memberService = new MemberService(memberRepository, passwordEncoder);
        authService = new AuthService(memberRepository, passwordEncoder, jwtProvider);
    }

    @Test
    public void 로그인_성공시_accessToken을_발급한다() throws Exception{
        //given
        memberService.createMember(
            "김철수",
            "kim@test.com",
            "password1234",
            30
        );
        //when
        LoginResponse response = authService.login("kim@test.com", "password1234");

        //then
        assertThat(response.memberId()).isNotNull();
        assertThat(response.email()).isEqualTo("kim@test.com");
        assertThat(response.accessToken()).isNotBlank();

        assertThat(jwtProvider.validateToken(response.accessToken())).isTrue();
        assertThat(jwtProvider.getMemberId(response.accessToken())).isEqualTo(response.memberId());
        assertThat(jwtProvider.getEmail(response.accessToken())).isEqualTo("kim@test.com");
    }

    @Test
    public void 존재하지_않는_이메일이면_로그인_실패한다() throws Exception{
        //when//then
        assertThatThrownBy(() ->
            authService.login("none@test.com", "password1234"))
            .isInstanceOf(LoginFailedException.class)
            .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");

    }

    @Test
    public void 비밀번호가_틀리면_로그인에_실패한다() throws Exception{
        //given
        memberService.createMember(
            "김철수",
            "kim@test.com",
            "password1234",
            30
        );
        //when//then
        assertThatThrownBy(() ->
            authService.login("kim@test.com", "wrongPassword1234"))
            .isInstanceOf(LoginFailedException.class)
            .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    public void 로그인_성공시_토큰에서_회원정보를_꺼낼_수_있다() throws Exception{
        //given
        memberService.createMember(
            "김철수",
            "kim@test.com",
            "password1234",
            30
        );
        //when
        LoginResponse response = authService.login("kim@test.com", "password1234");

        //then
        String accessToken = response.accessToken();

        assertThat(jwtProvider.getMemberId(accessToken)).isEqualTo(response.memberId());
        assertThat(jwtProvider.getEmail(accessToken)).isEqualTo("kim@test.com");
    }

    @Test
    public void 로그인_성공() throws Exception{
        //given
        memberService.createMember(
            "김철수",
            "kim@test.com",
            "password1234",
            30
        );
        //when
        LoginResponse response = authService.login("kim@test.com", "password1234");

        //then
        assertThat(response.memberId()).isNotNull();
        assertThat(response.email()).isEqualTo("kim@test.com");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(jwtProvider.validateToken(response.accessToken())).isTrue();
        assertThat(jwtProvider.getEmail(response.accessToken())).isEqualTo("kim@test.com");
        assertThat(jwtProvider.getRole(response.accessToken())).isEqualTo("USER");
    }

}
