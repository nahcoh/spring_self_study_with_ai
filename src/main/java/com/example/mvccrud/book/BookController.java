package com.example.mvccrud.book;

import com.example.mvccrud.global.ApiResponse;
import com.example.mvccrud.global.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Book API", description = "책 등록, 조회, 수정, 삭제, 검색 API")
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "책 등록", description = "책 제목과 가격을 입력받아 새 책을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
        @RequestBody @Valid BookCreateRequest request) {
        Book book = bookService.createBook(request.getTitle(), request.getPrice());
        ApiResponse<BookResponse> response = ApiResponse.of(new BookResponse(book));

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }


    @Operation(summary = "책 단건 조회", description = "ID로 책 한 권을 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<BookResponse> findBook(@PathVariable Long id) {
        Book book = bookService.findBook(id);
        return ApiResponse.of(new BookResponse(book));
    }

    @Operation(summary = "책 목록 조회", description = "책 목록을 페이징과 정렬 조건으로 조회합니다.")
    @GetMapping
    public ApiResponse<PageResponse<BookResponse>> findBooks(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
        Pageable pageable) {
        Page<BookResponse> books = bookService.findBooks(pageable)
            .map(BookResponse::new);

        return ApiResponse.of(PageResponse.from(books));
    }

    @Operation(summary = "책 전체 수정", description = "ID에 해당하는 책의 제목과 가격을 전체 수정합니다.")
    @PutMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(@PathVariable Long id,
        @RequestBody @Valid BookUpdateRequest request) {

        bookService.updateBook(id, request.getTitle(), request.getPrice());
        Book book = bookService.findBook(id);
        return ApiResponse.of(new BookResponse(book));
    }

    @Operation(summary = "책 부분 수정", description = "ID에 해당하는 책의 제목 또는 가격을 부분 수정합니다.")
    @PatchMapping("/{id}")
    public ApiResponse<BookResponse> patchBook(@PathVariable Long id,
        @RequestBody @Valid BookPatchRequest request) {

        Book book = bookService.patchBook(id, request.getTitle(), request.getPrice());
        return ApiResponse.of(new BookResponse(book));
    }

    @Operation(summary = "책 삭제", description = "ID에 해당하는 책을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {

        bookService.deleteBook(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "책 검색", description = "제목, 최소 가격, 최대 가격 조건으로 책을 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<PageResponse<BookResponse>> searchBooks(
        @ModelAttribute BookSearchRequest request,
        @PageableDefault(size = 10, sort = "id", direction = Direction.DESC)
        Pageable pageable) {

        Page<BookResponse> books = bookService.searchBooks(
            request.getTitle(),
            request.getMinPrice(),
            request.getMaxPrice(),
            pageable).map(BookResponse::new);

        return ApiResponse.of(PageResponse.from(books));
    }
}
