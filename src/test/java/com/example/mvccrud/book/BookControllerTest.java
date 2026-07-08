package com.example.mvccrud.book;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.mvccrud.global.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;


class BookControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        BookRepository bookRepository = new MemoryBookRepository();
        BookService bookService = new BookService(bookRepository);
        BookController bookController = new BookController(bookService);

        mockMvc = MockMvcBuilders
            .standaloneSetup(bookController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    public void 책_등록_API() throws Exception{
        //given
        BookCreateRequest request = new BookCreateRequest("데미안", 15000);

        //when
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.title").value("데미안"))
            .andExpect(jsonPath("$.data.price").value(15000));
        //then
    }

    @Test
    public void 책_등록_검증_실패() throws Exception{
        //given
        BookCreateRequest request = new BookCreateRequest("", 0);
        //when
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("검증에 실패했습니다."))
            .andExpect(jsonPath("$.errors").isArray());

        //then
    }

    @Test
    public void 책_단건_조회() throws Exception{
        //given
        BookCreateRequest request = new BookCreateRequest("데미안", 15000);
        //when
        String responseBody = mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long id = objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();

        mockMvc.perform(get("/books/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.title").value("데미안"))
            .andExpect(jsonPath("$.data.price").value(15000));

        //then
    }

    @Test
    public void 없는_책_조회() throws Exception{
        //given
        mockMvc.perform(get("/books/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("책을 찾을 수 없습니다."));

        //when

        //then
    }

    @Test
    public void 책_전체_조회() throws Exception{
        //given
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("데미안", 15000))));

        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("자바의 정석", 39000))));
        //when

        mockMvc.perform(get("/books")
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(5))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.data.first").value(true))
            .andExpect(jsonPath("$.data.last").value(true));
        //then
    }

    @Test
    public void 책_전체_수정() throws Exception{
        //given
        String responseBody = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookCreateRequest("데미안", 15000))))
            .andReturn()
            .getResponse()
            .getContentAsString();
        //when

        long id = objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();

        BookUpdateRequest request = new BookUpdateRequest("수정된 데미안", 20000);

        mockMvc.perform(put("/books/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("수정된 데미안"))
            .andExpect(jsonPath("$.data.price").value(20000));

        //then
    }

    @Test
    public void 책_부분_수정() throws Exception{
        //given
        String responseBody = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookCreateRequest("데미안", 15000))))
            .andReturn()
            .getResponse()
            .getContentAsString();

        long id = objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();
        //when

        BookPatchRequest request = new BookPatchRequest("제목만 수정", null);

        mockMvc.perform(patch("/books/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("제목만 수정"))
            .andExpect(jsonPath("$.data.price").value(15000));

        //then
    }

    @Test
    public void 책_삭제() throws Exception{
        //given
        String responseBody = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookCreateRequest("데미안", 15000))))
            .andReturn()
            .getResponse()
            .getContentAsString();
        //when
        Long id = objectMapper.readTree(responseBody)
            .get("data")
            .get("id")
            .asLong();

        mockMvc.perform(delete("/books/{id}", id))
            .andExpect(status().isNoContent());

        //then
    }

    @Test
    public void 책_제목_검색_API() throws Exception{
        //given
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("자바의 정석", 30000))));
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("스프링 입문", 25000))));
        //when

        mockMvc.perform(get("/books/search")
                .param("title", "자바")
                .param("page","0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].title").value("자바의 정석"))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(5))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.data.first").value(true))
            .andExpect(jsonPath("$.data.last").value(true));

        //then
    }

    @Test
    public void 첵_가격_범위_검색() throws Exception{
        //given
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("자바의 정석", 30000))));
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("스프링 입문", 25000))));
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("데미안", 12000))));
        //when

        mockMvc.perform(get("/books/search")
                .param("minPrice", "20000")
                .param("maxPrice", "30000")
                .param("page","0")
                .param("size","5"))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(5))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.data.first").value(true))
            .andExpect(jsonPath("$.data.last").value(true));

        //then
    }

    @Test
    public void 책_제목과_가격_검색_API() throws Exception{
        //given
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("자바의 정석", 30000))));
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("스프링 입문", 25000))));
        mockMvc.perform(post("/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BookCreateRequest("데미안", 12000))));

        //when

        mockMvc.perform(get("/books/search")
                .param("title", "자바")
                .param("minPrice", "20000")
                .param("maxPrice", "40000")
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(5))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.data.first").value(true))
            .andExpect(jsonPath("$.data.last").value(true));

        //then
    }


}