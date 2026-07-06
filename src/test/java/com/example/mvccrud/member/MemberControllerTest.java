package com.example.mvccrud.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mvccrud.global.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

class MemberControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MemberRepository memberRepository = new MemoryMemberRepository();
        MemberService memberService = new MemberService(memberRepository);
        MemberController memberController = new MemberController(memberService);

        mockMvc = MockMvcBuilders
            .standaloneSetup(memberController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    public void 회원_등록_API() throws Exception{
        //given
        MemberCreateRequest request = new MemberCreateRequest("김철수", "kim@test.com", 30);
        //when

        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.name").value("김철수"))
            .andExpect(jsonPath("$.data.email").value("kim@test.com"))
            .andExpect(jsonPath("$.data.age").value(30));

        //then
    }

    @Test
    public void 회원_등록_검증_실패() throws Exception{
        //given
        MemberCreateRequest request = new MemberCreateRequest("", "bad-email", 0);
        //when

        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("검증에 실패했습니다."))
            .andExpect(jsonPath("$.errors").isArray());

        //then
    }

    @Test
    public void 이메일_중복_등록_실패_API() throws Exception{
        //given
        mockMvc.perform(post("/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MemberCreateRequest("김철수", "kim@test.com", 30)
            )));
        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new MemberCreateRequest("이영희", "kim@test.com", 30)
                )))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }


    @Test
    public void 회원_단건_조회_API() throws Exception{
        //given
        String responseBody = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new MemberCreateRequest("김철수", "kim@test.com", 30)
                )))
            .andReturn()
            .getResponse()
            .getContentAsString();

        long id = objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();

        mockMvc.perform(get("/members/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.name").value("김철수"))
            .andExpect(jsonPath("$.data.email").value("kim@test.com"))
            .andExpect(jsonPath("$.data.age").value(30));
    }

    @Test
    public void 없는_회원_조회() throws Exception{
        //given
        mockMvc.perform(get("/members/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
    }

    @Test
    public void 회원_전체_조회_API() throws Exception{
        //given
        mockMvc.perform(post("/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MemberCreateRequest("김철수", "kim@test.com", 30)
            )));
        mockMvc.perform(post("/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MemberCreateRequest("이영희", "lee@test.com", 30)
            )));

        mockMvc.perform(get("/members"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    public void 회원_전체_수정_API() throws Exception{
        //given
        String responseBody = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new MemberCreateRequest("김철수", "kim@test.com", 300)
                )))
            .andReturn()
            .getResponse()
            .getContentAsString();

        long id = objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();

        MemberUpdateRequest request = new MemberUpdateRequest("수정된 이름", "new@test.com", 35);

        mockMvc.perform(put("/members/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("수정된 이름"))
            .andExpect(jsonPath("$.data.email").value("new@test.com"))
            .andExpect(jsonPath("$.data.age").value(35));
        //when

        //then
    }

    @Test
    public void 회원_부분_수정_API() throws Exception{
        //given
        String responseBody = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new MemberCreateRequest("김철수", "kim@test.com", 30)
                )))
            .andReturn()
            .getResponse()
            .getContentAsString();

        long id = objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();

        MemberPatchRequest request = new MemberPatchRequest("이름만 수정", null, null);

        mockMvc.perform(patch("/members/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("이름만 수정"))
            .andExpect(jsonPath("$.data.email").value("kim@test.com"))
            .andExpect(jsonPath("$.data.age").value(30));
        //when

        //then
    }

    @Test
    public void 회원_삭제_API() throws Exception{
        //given
        String responseBody = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new MemberCreateRequest("김철수", "kim@test.com", 30)
                )))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long id = objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();

        //when
        mockMvc.perform(delete("/members/{id}", id))
            .andExpect(status().isNoContent());
        //then
    }

    @Test
    public void 회원_검색_API() throws Exception{
        //given
        mockMvc.perform(post("/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MemberCreateRequest("김철수", "kim@test.com", 30)
            )));
        mockMvc.perform(post("/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MemberCreateRequest("김영희", "young@naver.com", 30)
            )));
        mockMvc.perform(post("/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MemberCreateRequest("박민수", "park@test.com", 30)
            )));
        //when
        mockMvc.perform(get("/members/search")
                .param("name", "김")
                .param("email", "test.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].name").value("김철수"));

        //then
    }
}