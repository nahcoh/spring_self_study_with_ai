package com.example.mvccrud.member;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member createMember(String name, String email, int age) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        Member member = new Member(null, name, email, age);
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

}
