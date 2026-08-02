package com.example.mvccrud.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mvccrud.auth.LoginRequest;
import com.example.mvccrud.book.Book;
import com.example.mvccrud.book.BookService;
import com.example.mvccrud.member.Member;
import com.example.mvccrud.member.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderSecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberService memberService;
    @Autowired BookService bookService;

    @Test
    public void 토큰_없이_주문_생성하면_401이_뜬다() throws Exception{
        //given
        Member member = memberService.createMember(
            "김철수",
            "kim@test.com",
            "password1234",
            30
        );

        Book book = bookService.createBook("데미안", 15000);

        OrderCreateRequest request = new OrderCreateRequest(
            book.getId(),
            2
        );
        //when//then

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

    }

    @Test
    public void 토큰이_있으면_주문_생성에_성공한다() throws Exception{
        //given
        Member member = memberService.createMember(
            "김철수",
            "security-order-token@test.com",
            "password1234",
            30
        );

        Book book = bookService.createBook("데미안", 15000);

        LoginRequest loginRequest = new LoginRequest(
            "security-order-token@test.com",
            "password1234"
        );

        String loginResponseBody = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String accessToken = objectMapper.readTree(loginResponseBody)
            .get("data")
            .get("accessToken")
            .asText();

        OrderCreateRequest orderRequest = new OrderCreateRequest(
            book.getId(),
            2
        );

        //when//then
        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isCreated())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.memberId").value(member.getId()));
    }





}
