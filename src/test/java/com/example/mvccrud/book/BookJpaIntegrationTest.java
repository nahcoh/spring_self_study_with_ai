package com.example.mvccrud.book;

import static org.assertj.core.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BookJpaIntegrationTest {

    @Autowired BookService bookService;
    @Autowired EntityManager em;

    @Test
    void 책_등록시_DB에_저장된다() {
        //given
        Book book = bookService.createBook("데미안", 15000);

        //when
        Book foundBook = em.find(Book.class, book.getId());

        //then
        assertThat(foundBook.getId()).isEqualTo(book.getId());
        assertThat(foundBook.getTitle()).isEqualTo("데미안");
        assertThat(foundBook.getPrice()).isEqualTo(15000);
    }

    @Test
    public void 책_조회가_된다() throws Exception{
        //given
        Book savedBook = bookService.createBook("자바의 정석", 30000);
        //when
        Book foundBook = bookService.findBook(savedBook.getId());

        //then
        assertThat(foundBook.getId()).isEqualTo(savedBook.getId());
        assertThat(foundBook.getTitle()).isEqualTo("자바의 정석");
        assertThat(foundBook.getPrice()).isEqualTo(30000);
    }

    @Test
    public void 없는_책_조회_실패() throws Exception{
        //when//then
        assertThatThrownBy(() -> bookService.findBook(999L))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessage("책을 찾을 수 없습니다.");
    }

    @Test
    public void 책_수정시_변경감지가_동작한다() throws Exception{
        //given
        Book savedBook = bookService.createBook("데미안", 15000);

        //when
        bookService.updateBook(savedBook.getId(), "수정된 데미안", 20000);

        Book foundBook = em.find(Book.class, savedBook.getId());

        //then
        assertThat(foundBook.getTitle()).isEqualTo("수정된 데미안");
        assertThat(foundBook.getPrice()).isEqualTo(20000);
    }

    @Test
    public void 책_부분수정이_된다() throws Exception{
        //given
        Book savedBook = bookService.createBook("데미안", 15000);
        //when
        bookService.patchBook(savedBook.getId(), "제목만 수정", null);

        //then
        Book foundBook = em.find(Book.class, savedBook.getId());
        assertThat(foundBook.getTitle()).isEqualTo("제목만 수정");
        assertThat(foundBook.getPrice()).isEqualTo(15000);
    }

    @Test
    public void 책_삭제가_된다() throws Exception{
        //given
        Book savedBook = bookService.createBook("데미안", 15000);
        //when
        bookService.deleteBook(savedBook.getId());

        //then
        Book foundBook = em.find(Book.class, savedBook.getId());
        assertThat(foundBook).isNull();
    }

    @Test
    public void 책_검색이_된다() throws Exception{
        //given
        bookService.createBook("자바의 정석", 30000);
        bookService.createBook("스프링 입문", 25000);
        bookService.createBook("데미안", 12000);
        //when
        List<Book> books = bookService.searchBooks("자바", 20000, 40000);

        //then
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("자바의 정석");

    }

    @Test
    public void 가격_범위로_검색이_된다() throws Exception{
        //given
        bookService.createBook("자바의 정석", 30000);
        bookService.createBook("스프링 입문", 25000);
        bookService.createBook("데미안", 12000);

        //when
        List<Book> books = bookService.searchBooks(null, 20000, 30000);

        //then
        assertThat(books).hasSize(2);
    }

}