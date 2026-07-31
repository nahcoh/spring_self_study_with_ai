package com.example.mvccrud.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mvccrud.global.GlobalExceptionHandler;
import com.example.mvccrud.global.security.JwtProvider;
import com.example.mvccrud.member.MemberRepository;
import com.example.mvccrud.member.MemberService;
import com.example.mvccrud.member.MemoryMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

public class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MemoryMemberRepository memberRepository = new MemoryMemberRepository();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        JwtProvider jwtProvider = new JwtProvider(
            "this-is-a-very-long-secret-key-for-jwt-token-signing-practice-2026",
            3600L
        );

        memberService = new MemberService(memberRepository, bCryptPasswordEncoder);
        AuthService authService = new AuthService(memberRepository, bCryptPasswordEncoder,
            jwtProvider);
        AuthController authController = new AuthController(authService);

        mockMvc = MockMvcBuilders
            .standaloneSetup(authController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void 로그인_성공_API() throws Exception {
        // given
        memberService.createMember(
            "김철수",
            "kim@test.com",
            "password1234",
            30
        );

        LoginRequest request = new LoginRequest(
            "kim@test.com",
            "password1234"
        );

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.memberId").exists())
            .andExpect(jsonPath("$.data.email").value("kim@test.com"))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void 존재하지_않는_이메일이면_로그인_실패_API() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
            "none@test.com",
            "password1234"
        );

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 비밀번호가_틀리면_로그인_실패_API() throws Exception {
        // given
        memberService.createMember(
            "김철수",
            "kim@test.com",
            "password1234",
            30
        );

        LoginRequest request = new LoginRequest(
            "kim@test.com",
            "wrong-password"
        );

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 로그인_요청_검증_실패_API() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
            "",
            ""
        );

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("검증에 실패했습니다."))
            .andExpect(jsonPath("$.errors").isArray());
    }
}
