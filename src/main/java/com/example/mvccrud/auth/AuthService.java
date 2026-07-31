package com.example.mvccrud.auth;

import com.example.mvccrud.global.security.JwtProvider;
import com.example.mvccrud.member.Member;
import com.example.mvccrud.member.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider =jwtProvider;
    }

    public LoginResponse login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new LoginFailedException();
        }

        String accessToken = jwtProvider.createAccessToken(member);

        return new LoginResponse(
            member.getId(),
            member.getEmail(),
            accessToken
        );
    }

}
