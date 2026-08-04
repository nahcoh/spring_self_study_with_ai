package com.example.mvccrud.member;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member createMember(String name, String email, String password, int age) {
        return createMemberWithRole(name, email, password, age, Role.USER);
    }

    @Transactional
    public Member createAdminMember(String name, String email, String password, int age) {
        return createMemberWithRole(name, email, password, age, Role.ADMIN);
    }

    private Member createMemberWithRole(String name, String email, String password, int age,
        Role role) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        String encodedPassword = passwordEncoder.encode(password);

        Member member = new Member(
            name, email, encodedPassword, age, role
        );

        return memberRepository.save(member);
    }

    public Member findMember(Long id) {
        return memberRepository.findById(id).orElseThrow(MemberNotFoundException::new);
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Page<Member> findMembers(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    public Page<MemberResponse> findMemberResponses(Pageable pageable) {
        return memberRepository.findAll(pageable)
            .map(MemberResponse::new);
    }

    public Member updateMember(Long id, String name, String email, int age) {
        Member member = findMember(id);
        //새로운 이메일이 기존 이메일과 동일하면 예외 발생
        if (!member.getEmail().equals(email) && memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        member.changeName(name);
        member.changeEmail(email);
        member.changeAge(age);

        return member;
    }

    public Member patchMember(Long id, String name, String email, Integer age) {
        Member member = findMember(id);

        if (name != null) {
            member.changeName(name);
        }
        if (email != null) {
            if (!member.getEmail().equals(email) && memberRepository.existsByEmail(email)) {
                throw new DuplicateEmailException();
            }
            member.changeEmail(email);
        }
        if (age != null) {
            member.changeAge(age);
        }
        return member;
    }

    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new MemberNotFoundException();
        }
        memberRepository.deleteById(id);
    }

    public List<Member> searchMembers(String name, String email) {
        return memberRepository.search(name, email);
    }

    public Page<Member> searchMembers(String name, String email, Pageable pageable) {
        return memberRepository.search(name, email, pageable);
    }
    
}
