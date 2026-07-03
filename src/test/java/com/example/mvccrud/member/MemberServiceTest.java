package com.example.mvccrud.member;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemberServiceTest {

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MemberRepository memberRepository = new MemoryMemberRepository();
        memberService = new MemberService(memberRepository);

    }

    @Test
    public void 회원_등록_성공() throws Exception{
        //given
        String name = "김철수";
        String email = "kim@test.com";
        int age = 30;

        //when
        Member member = memberService.createMember(name, email, age);

        //then
        assertThat(member.getId()).isNotNull();
        assertThat(member.getName()).isEqualTo("김철수");
        assertThat(member.getEmail()).isEqualTo("kim@test.com");
        assertThat(member.getAge()).isEqualTo(30);

    }

    @Test
    public void 이메일_중복_등록_실패() throws Exception{
        //given
        memberService.createMember("김철수", "kim@test.com", 30);

        //when//then
        assertThatThrownBy(() ->
            memberService.createMember("이영희", "kim@test.com", 25)
        )
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    public void 회원_단건_조회_성공() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", 30);

        //when
        Member foundMember = memberService.findMember(member.getId());

        //then
        assertThat(foundMember.getId()).isEqualTo(member.getId());
        assertThat(foundMember.getName()).isEqualTo("김철수");
        assertThat(foundMember.getAge()).isEqualTo(30);
        assertThat(foundMember.getEmail()).isEqualTo("kim@test.com");

    }

    @Test
    public void 없는_회원_조회_실패() throws Exception{
        //given

        //when//then
        assertThatThrownBy(() -> memberService.findMember(999L))
            .isInstanceOf(MemberNotFoundException.class)
            .hasMessage("회원을 찾을 수 없습니다.");
    }

    @Test
    public void 회원_전체_조회() throws Exception{
        //given
        memberService.createMember("김철수", "kim@test.com", 30);
        memberService.createMember("이영희", "lee@test.com", 25);

        //when
        List<Member> members = memberService.findMembers();

        //then
        assertThat(members).hasSize(2);
    }

    @Test
    public void 회원_전체_수정_PUT() throws Exception{
        //given
        Member savedMember = memberService.createMember("김철수", "kim@test.com", 30);
        //when
        Member updatedMember = memberService.updateMember(savedMember.getId(), "수정된 이름", "new@test.com",
            35);

        //then
        assertThat(updatedMember.getName()).isEqualTo("수정된 이름");
        assertThat(updatedMember.getEmail()).isEqualTo("new@test.com");
        assertThat(updatedMember.getAge()).isEqualTo(35);
    }

    @Test
    public void 회원_전체_수정시_이메일_중복_실패() throws Exception{
        //given
        Member member1 = memberService.createMember("김철수", "kim@test.com",30);
        memberService.createMember("이영희", "lee@test.com", 25);
        //when&then
        assertThatThrownBy(() ->
            memberService.updateMember(
                member1.getId(),
                "김철수",
                "lee@test.com",
                30
            ))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    public void 회원_이름만_부분수정_PATHC() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", 30);
        //when
        Member patchedMember = memberService.patchMember(
            member.getId(),
            "이름만 수정",
            null,
            null
        );

        //then
        assertThat(patchedMember.getName()).isEqualTo("이름만 수정");
        assertThat(patchedMember.getEmail()).isEqualTo("kim@test.com");
        assertThat(patchedMember.getAge()).isEqualTo(30);

    }
    @Test
    public void 회원_이메일만_부분수정_PATHC() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", 30);
        //when
        Member patchedMember = memberService.patchMember(
            member.getId(),
            null,
            "new@test.com",
            null
        );

        //then
        assertThat(patchedMember.getName()).isEqualTo("김철수");
        assertThat(patchedMember.getEmail()).isEqualTo("new@test.com");
        assertThat(patchedMember.getAge()).isEqualTo(30);

    }

    @Test
    public void 회원_나이만_부분수정_PATHC() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", 30);
        //when
        Member patchedMember = memberService.patchMember(
            member.getId(),
            null,
            null,
            25
        );

        //then
        assertThat(patchedMember.getName()).isEqualTo("김철수");
        assertThat(patchedMember.getEmail()).isEqualTo("kim@test.com");
        assertThat(patchedMember.getAge()).isEqualTo(25);

    }

    @Test
    public void 회원_부분수정시_이메일_중복_실패() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", 30);
        memberService.createMember("이영희", "lee@test.com", 25);
        //when
        assertThatThrownBy(() ->
            memberService.patchMember(
                member.getId(),
                null,
                "lee@test.com",
                null
            ))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessage("이미 사용 중인 이메일입니다.");

        //then
    }

    @Test
    public void 회원_삭제_성공() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", 30);
        //when
        memberService.deleteMember(member.getId());

        //then
        assertThatThrownBy(() -> memberService.findMember(member.getId()))
            .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    public void 없는_회원_삭제_실패() throws Exception{
        //given

        //when

        //then
        assertThatThrownBy(() -> memberService.deleteMember(999L))
            .isInstanceOf(MemberNotFoundException.class)
            .hasMessage("회원을 찾을 수 없습니다.");

    }

    @Test
    public void 이름으로_회원_검색() throws Exception{
        //given
        memberService.createMember("박민수", "park@test.com", 20);
        memberService.createMember("김철수", "kim@test.com", 30);
        memberService.createMember("김영희", "yong@test.com", 25);
        //when
        List<Member> members = memberService.searchMembers("김",null);
        //then
        assertThat(members).hasSize(2);

    }

    @Test
    public void 이메일로_회원_검색() throws Exception{
        //given
        memberService.createMember("박민수", "park@test.com", 20);
        memberService.createMember("김철수", "kim@test.com", 30);
        memberService.createMember("김영희", "yong@test.com", 25);
        //when
        List<Member> members = memberService.searchMembers(null,"test.com");
        //then
        assertThat(members).hasSize(3);

    }

    @Test
    public void 이름과_이메일로_회원_검색() throws Exception{
        //given
        memberService.createMember("박민수", "park@test.com", 20);
        memberService.createMember("김철수", "kim@test.com", 30);
        memberService.createMember("김영희", "yong@naver.com", 25);
        //when
        List<Member> members = memberService.searchMembers("김","test.com");
        //then
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getName()).isEqualTo("김철수");

    }
}