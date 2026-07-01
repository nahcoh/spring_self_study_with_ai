package com.example.mvccrud.book;


import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public BookResponse createBook(@RequestBody @Valid BookCreateRequest request) {
        Book book = bookService.createBook(request.getTitle(), request.getPrice());
        return new BookResponse(book);
    }

    @GetMapping("/{id}")
    public BookResponse findBook(@PathVariable Long id) {
        Book book = bookService.findBook(id);
        return new BookResponse(book);
    }

    @GetMapping
    public List<BookResponse> findBooks() {
        return bookService.findBooks().stream()
            .map(BookResponse::new)
            .toList();
    }

    @PatchMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id,
        @RequestBody @Valid BookUpdateRequest request) {

        bookService.updateBook(id, request.getTitle(), request.getPrice());
        Book book = bookService.findBook(id);
        return new BookResponse(book);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);

    }
}
