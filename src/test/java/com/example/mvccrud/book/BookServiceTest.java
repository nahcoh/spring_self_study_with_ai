package com.example.mvccrud.book;

import static org.assertj.core.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookServiceTest {

    private BookService bookService;

    @BeforeEach
    void setUp() {
        MemoryBookRepository memoryBookRepository = new MemoryBookRepository();
        bookService = new BookService(memoryBookRepository);
    }

    @Test
    public void 책_등록_성공() throws Exception {
        //given
        String title = "데미안";
        int price = 15000;

        //when
        Book book = bookService.createBook(title, price);

        //then
        assertThat(book.getId()).isNotNull();
        assertThat(book.getTitle()).isEqualTo("데미안");
        assertThat(book.getPrice()).isEqualTo(15000);
    }

    @Test
    public void 책_단건_조회_성공() throws Exception{
        //given
        Book savedBook = bookService.createBook("데미안", 15000);

        //when
        Book foundBook = bookService.findBook(savedBook.getId());

        //then
        assertThat(foundBook.getId()).isEqualTo(savedBook.getId());
        assertThat(foundBook.getTitle()).isEqualTo("데미안");
        assertThat(foundBook.getPrice()).isEqualTo(15000);
    }

    @Test
    public void 없는_책_조회_실패() throws Exception{
        //given


        //when//then
        assertThatThrownBy(() -> bookService.findBook(999L))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessage("책을 찾을 수 없습니다.");

    }

    @Test
    public void 책_전체_조회() throws Exception{
        //given
        bookService.createBook("데미안", 15000);
        bookService.createBook("자바의 정석", 30000);

        //when
        List<Book> books = bookService.findBooks();

        //then
        assertThat(books).hasSize(2);
    }

    @Test
    public void 책_전체_수정_PUT() throws Exception{
        //given
        Book savedBook = bookService.createBook("데미안", 15000);

        //when
        Book updatedBook = bookService.updateBook(savedBook.getId(), "수정된 데미안", 20000);

        //then
        assertThat(updatedBook.getTitle()).isEqualTo("수정된 데미안");
        assertThat(updatedBook.getPrice()).isEqualTo(20000);
    }

    @Test
    public void 책_제목만_부분수정_patch() throws Exception{
        //given
        Book savedBook = bookService.createBook("데미안", 15000);
        //when
        Book patchedBook = bookService.patchBook(savedBook.getId(), "제목만 수정", null);

        //then
        assertThat(patchedBook.getTitle()).isEqualTo("제목만 수정");
        assertThat(patchedBook.getPrice()).isEqualTo(15000);
    }

    @Test
    public void 책_가격만_부분수정_patch() throws Exception{
        //given
        Book savedBook = bookService.createBook("데미안", 15000);

        //when
        Book patchedBook = bookService.patchBook(savedBook.getId(), null, 25000);

        //then
        assertThat(patchedBook.getTitle()).isEqualTo("데미안");
        assertThat(patchedBook.getPrice()).isEqualTo(25000);
    }

    @Test
    public void 책_삭제_성공() throws Exception{
        //given
        Book savedBook = bookService.createBook("데미안", 15000);
        //when
        bookService.deleteBook(savedBook.getId());
        //then
        assertThatThrownBy(() -> bookService.findBook(savedBook.getId()))
            .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    public void 없는_책_삭제_실패() throws Exception{
        //given

        //when//then
        assertThatThrownBy(() -> bookService.deleteBook(999L))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessage("책을 찾을 수 없습니다.");

    }
}
