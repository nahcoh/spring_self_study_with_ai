package com.example.mvccrud.member;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberJpaIntegrationTest {

    @Autowired MemberService memberService;
    @Autowired EntityManager em;


    @Test
    public void 회원_등록시_DB에_저장된다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com",30);
        //when
        Member foundMember = em.find(Member.class, member.getId());
        //then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getId()).isEqualTo(member.getId());
        assertThat(foundMember.getName()).isEqualTo("김철수");
        assertThat(foundMember.getEmail()).isEqualTo("kim@test.com");
        assertThat(foundMember.getAge()).isEqualTo(30);
    }

    @Test
    public void 회원_조회가_된다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com",30);
        //when
        Member foundMember = memberService.findMember(member.getId());
        //then
        assertThat(foundMember.getId()).isEqualTo(member.getId());
        assertThat(foundMember.getName()).isEqualTo("김철수");
        assertThat(foundMember.getEmail()).isEqualTo("kim@test.com");
        assertThat(foundMember.getAge()).isEqualTo(30);
    }

    @Test
    public void 없는_회원_조회_실패() throws Exception{
        //when//then
        assertThatThrownBy(() -> memberService.findMember(999L))
            .isInstanceOf(MemberNotFoundException.class)
            .hasMessage("회원을 찾을 수 없습니다.");
    }

    @Test
    public void 이메일_중복_등록_실패() throws Exception{
        //given
        memberService.createMember("김철수", "kim@test.com",30);

        //when//then
        assertThatThrownBy(() ->
            memberService.createMember("이영흐", "kim@test.com", 25))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    public void 회원_수정시_변경감지가_동작한다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com",30);

        //when
        memberService.updateMember(
            member.getId(),
            "수정된 이름",
            "new@test.com",
            35
        );

        //then
        Member foundMember = em.find(Member.class, member.getId());

        assertThat(foundMember.getName()).isEqualTo("수정된 이름");
        assertThat(foundMember.getEmail()).isEqualTo("new@test.com");
        assertThat(foundMember.getAge()).isEqualTo(35);
    }

    @Test
    public void 회원_부분수정이_된다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com",30);

        //when
        memberService.patchMember(member.getId(), "이름만 수정", null, null);
        //then
        Member foundMember = em.find(Member.class, member.getId());

        assertThat(foundMember.getName()).isEqualTo("이름만 수정");
        assertThat(foundMember.getEmail()).isEqualTo("kim@test.com");
        assertThat(foundMember.getAge()).isEqualTo(30);
    }

    @Test
    public void 회원_삭제가_된다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com",30);

        //when
        memberService.deleteMember(member.getId());

        //then
        Member foundMember = em.find(Member.class, member.getId());
        assertThat(foundMember).isNull();
    }

    @Test
    public void 이름으로_회원_검색이_된다() throws Exception{
        //given
        memberService.createMember("김철수", "kim@test.com",30);
        memberService.createMember("김영희", "kim@naver.com", 30);
        memberService.createMember("박민수", "park@test.com", 30);

        //when
        List<Member> members = memberService.searchMembers("김", null);
        //then
        assertThat(members).hasSize(2);
    }

    @Test
    public void 이메일로_회원_검색이_된다() throws Exception{
        //given
        memberService.createMember("김철수", "kim@test.com",30);
        memberService.createMember("김영희", "kim@naver.com", 30);
        memberService.createMember("박민수", "park@test.com", 30);

        //when
        List<Member> members = memberService.searchMembers(null, "test.com");

        //then
        assertThat(members).hasSize(2);
    }

    @Test
    public void 이름과_이메일로_회원_검색이_된다() throws Exception{
        //given
        memberService.createMember("김철수", "kim@test.com",30);
        memberService.createMember("김영희", "kim@naver.com", 30);
        memberService.createMember("박민수", "park@test.com", 30);

        //when
        List<Member> members = memberService.searchMembers("김", "test.com");

        //then
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getName()).isEqualTo("김철수");
    }

}