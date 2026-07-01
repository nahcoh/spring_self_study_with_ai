package com.example.mvccrud.book;


import com.example.mvccrud.global.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@RequestBody @Valid BookCreateRequest request) {
        Book book = bookService.createBook(request.getTitle(), request.getPrice());
        ApiResponse<BookResponse>  response = ApiResponse.of(new BookResponse(book));

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }



    @GetMapping("/{id}")
    public ApiResponse<BookResponse> findBook(@PathVariable Long id) {
        Book book = bookService.findBook(id);
        return ApiResponse.of(new BookResponse(book));
    }

    @GetMapping
    public ApiResponse<List<BookResponse>> findBooks() {
        List<BookResponse> books = bookService.findBooks().stream()
            .map(BookResponse::new)
            .toList();

        return ApiResponse.of(books);
    }

    @PatchMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(@PathVariable Long id,
        @RequestBody @Valid BookUpdateRequest request) {

        bookService.updateBook(id, request.getTitle(), request.getPrice());
        Book book = bookService.findBook(id);
        return ApiResponse.of(new BookResponse(book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);

        return ResponseEntity.noContent().build();
    }

}
