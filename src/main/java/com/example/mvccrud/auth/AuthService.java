package com.example.mvccrud.auth;

import com.example.mvccrud.member.Member;
import com.example.mvccrud.member.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new LoginFailedException();
        }

        return new LoginResponse(
            member.getId(),
            member.getEmail(),
            "로그인 성공"
        );
    }

}
