package com.example.mvccrud.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mvccrud.book.BookController;
import com.example.mvccrud.book.BookService;
import com.example.mvccrud.book.MemoryBookRepository;
import com.example.mvccrud.global.GlobalExceptionHandler;
import com.example.mvccrud.global.SortValidator;
import com.example.mvccrud.global.security.JwtAuthenticationFilter;
import com.example.mvccrud.global.security.JwtProvider;
import com.example.mvccrud.member.MemberController;
import com.example.mvccrud.member.MemberService;
import com.example.mvccrud.member.MemoryMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

class OrderControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private int memberSequence = 0;

    @BeforeEach
    void setUp() {
        MemoryMemberRepository memberRepository = new MemoryMemberRepository();
        MemoryBookRepository bookRepository = new MemoryBookRepository();
        MemoryOrderRepository orderRepository = new MemoryOrderRepository();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        MemberService memberService = new MemberService(memberRepository, passwordEncoder);
        BookService bookService = new BookService(bookRepository);
        OrderService orderService = new OrderService(orderRepository, memberService, bookService);

        MemberController memberController = new MemberController(memberService, new SortValidator());
        BookController bookController = new BookController(bookService, new SortValidator());
        OrderController orderController = new OrderController(orderService, new SortValidator());


        mockMvc = MockMvcBuilders
            .standaloneSetup(memberController, bookController, orderController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    public void 주문_생성_API() throws Exception {
        Long memberId = createMember();
        Long bookId = createBook();

        OrderCreateRequest request = new OrderCreateRequest(memberId, bookId, 2);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.memberId").value(memberId))
            .andExpect(jsonPath("$.data.bookId").value(bookId))
            .andExpect(jsonPath("$.data.quantity").value(2))
            .andExpect(jsonPath("$.data.orderPrice").value(15000))
            .andExpect(jsonPath("$.data.totalPrice").value(30000))
            .andExpect(jsonPath("$.data.status").value("ORDERED"));
    }

    @Test
    public void 주문_생성_검증_실패_API() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest(null, null, 0);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("검증에 실패했습니다."))
            .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    public void 없는_회원으로_주문_생성_실패() throws Exception {
        Long bookId = createBook();
        OrderCreateRequest request = new OrderCreateRequest(999L, bookId, 2);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
    }

    @Test
    public void 없는_책으로_주문_생성_실패_API() throws Exception {
        Long memberId = createMember();
        OrderCreateRequest request = new OrderCreateRequest(memberId, 888L, 2);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("책을 찾을 수 없습니다."));
    }

    @Test
    public void 주문_단건_조회_API() throws Exception {
        Long orderId = createOrder(createMember(), createBook(), 2);

        mockMvc.perform(get("/orders/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(orderId))
            .andExpect(jsonPath("$.data.status").value("ORDERED"));
    }

    @Test
    public void 없는_주문_조회_API() throws Exception {
        mockMvc.perform(get("/orders/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("주문을 찾을 수 없습니다."));
    }

    @Test
    public void 주문_전체_조회_API() throws Exception {
        Long memberId = createMember();
        Long bookId = createBook();

        createOrder(memberId, bookId, 2);
        createOrder(memberId, bookId, 1);

        mockMvc.perform(get("/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    public void 주문_취소_API() throws Exception {
        Long orderId = createOrder(createMember(), createBook(), 2);

        mockMvc.perform(patch("/orders/{id}/cancel", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(orderId))
            .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    @Test
    public void 이미_취소된_주문_다시_취소_실패_API() throws Exception {
        Long orderId = createOrder(createMember(), createBook(), 2);

        mockMvc.perform(patch("/orders/{id}/cancel", orderId))
            .andExpect(status().isOk());

        mockMvc.perform(patch("/orders/{id}/cancel", orderId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("이미 취소된 주문입니다."));
    }

    @Test
    public void 주문_검색_API() throws Exception {
        Long memberId = createMember();
        Long bookId = createBook();

        Long orderId1 = createOrder(memberId, bookId, 1);
        createOrder(createMember(), createBook(), 2);

        mockMvc.perform(patch("/orders/{id}/cancel", orderId1))
            .andExpect(status().isOk());

        mockMvc.perform(get("/orders/search")
                .param("memberId", String.valueOf(memberId))
                .param("status", "CANCELED")
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].status").value("CANCELED"));
    }

    private Long createMember() throws Exception {
        memberSequence++;

        String responseBody = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "김철수",
                      "email": "kim%s@test.com",
                      "password": "password1234",
                      "age": 30
                    }
                    """.formatted(memberSequence)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();
    }

    private Long createBook() throws Exception {
        String responseBody = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "데미안",
                      "price": 15000
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();
    }

    private Long createOrder(Long memberId, Long bookId, int quantity) throws Exception {
        OrderCreateRequest request = new OrderCreateRequest(memberId, bookId, quantity);

        String responseBody = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();
    }
}