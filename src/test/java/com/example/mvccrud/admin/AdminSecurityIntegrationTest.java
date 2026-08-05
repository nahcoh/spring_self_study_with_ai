package com.example.mvccrud.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mvccrud.auth.LoginRequest;
import com.example.mvccrud.book.Book;
import com.example.mvccrud.book.BookService;
import com.example.mvccrud.member.Member;
import com.example.mvccrud.member.MemberService;
import com.example.mvccrud.order.Order;
import com.example.mvccrud.order.OrderCreateRequest;
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
class AdminSecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MemberService memberService;
    @Autowired
    private BookService bookService;

    @Test
    public void 토큰_없이_관리자_회원_조회하면_401이_나온다() throws Exception{
        //given
        mockMvc.perform(get("/admin/members"))
            .andExpect(status().isUnauthorized());
        //when

        //then
    }

    @Test
    public void 일반_USER는_관리자_회원_조회할_수_없다() throws Exception{
        //given
        memberService.createMember(
            "일반회원",
            "user-admin-test@test.com",
            "password",
            30
        );

        String userToken = loginAndGetAccessToken("user-admin-test@test.com");
        //when
        //then
        mockMvc.perform(get("/admin/members")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    public void ADMIN은_관리자_회원_조회할_수_있다() throws Exception{
        //given
        memberService.createAdminMember(
            "관리자",
            "admin@test.com",
            "password",
            40
        );

        memberService.createMember(
            "일반회원",
            "normal@test.com",
            "password",
            30
        );

        String adminToken = loginAndGetAccessToken("admin@test.com");

        //when//then
        mockMvc.perform(get("/admin/members")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.content[0].email").exists());
    }

    private String loginAndGetAccessToken(String email) throws Exception {
        LoginRequest loginRequest = new LoginRequest(
            email,
            "password"
        );

        String responseBody = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest
                )))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(responseBody)
            .get("data")
            .get("accessToken")
            .asText();
    }
    
    @Test
    public void 토큰_없이_관리자_주문_조회하면_401이_나온다() throws Exception{
        //given
        mockMvc.perform(get("/admin/members"))
            .andExpect(status().isUnauthorized());
        //when
        
        //then
    }

    @Test
    public void USER는_관리자_주문_조회할_수_없다() throws Exception{
        //given
        Member user = makeUser();

        String userToken = loginAndGetAccessToken(user.getEmail());
        //when
        //then
        mockMvc.perform(get("/admin/orders")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    public void ADMIN은_관리자로_주문_조회할_수_있다() throws Exception{
        //given
        Member admin = makeAdmin();
        Member user = makeUser();

        Book book = bookService.createBook("ㄷㅁㅇ", 15000);

        String adminToken = loginAndGetAccessToken(admin.getEmail());
        String userToken = loginAndGetAccessToken(user.getEmail());

        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OrderCreateRequest(book.getId(), 2)
                )))
            .andExpect(status().isCreated());


        //when
        //then
        mockMvc.perform(get("/admin/orders")
                .header("Authorization", "Bearer " + adminToken)
                .param("pagee","0")
                .param("size","10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].memberName").value("일반회원"))
            .andExpect(jsonPath("$.data.content[0].bookTitle").value("ㄷㅁㅇ"));
    }

    @Test
    public void 토큰_없이_관리자_주문_검색하면_401이_나온다() throws Exception{
        //given
        mockMvc.perform(get("/admin/orders/search")
                .param("stauts", "ORDERED"))
            .andExpect(status().isUnauthorized());
        //when

        //then
    }

    @Test
    public void USER는_관리자_주문_검색할_수_없다() throws Exception{
        //given
        Member user = makeUser();

        String userToken = loginAndGetAccessToken(user.getEmail());
        //when//then
        mockMvc.perform(get("/admin/orders/search")
                .header("Authorization", "Bearer " + userToken)
                .param("status", "ORDERED"))
            .andExpect(status().isForbidden());
    }

    @Test
    public void ADMIN은_주문_상태로_검색할_수_있다() throws Exception{
        //given
        Member admin = makeAdmin();
        Member user = makeUser();

        Book book = bookService.createBook("데미안", 15000);
        String adminToken = loginAndGetAccessToken(admin.getEmail());
        String userToken = loginAndGetAccessToken(user.getEmail());

        String orderResponseBody = mockMvc.perform(post("/orders")
            .header("Authorization", "Bearer " +userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new OrderCreateRequest(book.getId(), 2)
            )))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long orderId = objectMapper.readTree(orderResponseBody)
            .get("data")
            .get("id")
            .asLong();

        mockMvc.perform(patch("/orders/{id}/cancel", orderId)
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk());

        mockMvc.perform(get("/admin/orders/search")
                .header("Authorization","Bearer "+adminToken)
                .param("status", "CANCELED")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].status").value("CANCELED"))
            .andExpect(jsonPath("$.data.content[0].memberName").value("일반회원"));

        //when

        //then
    }


    public Member makeUser() {
        Member user = memberService.createMember(
            "일반회원",
            "admin-order-user@test.com",
            "password",
            30
        );
        return user;
    }
    public Member makeAdmin() {
        Member admin = memberService.createAdminMember(
            "관리자",
            "admin-order-admin@test.com",
            "password",
            30
        );
        return admin;
    }

}
