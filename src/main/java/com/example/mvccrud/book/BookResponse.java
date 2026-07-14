package com.example.mvccrud.book;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BookResponse {

    @Schema(description = "책 ID", example = "1")
    private final Long id;

    @Schema(description = "책 제목", example = "데미안")
    private final String title;

    @Schema(description = "책 가격", example = "15000")
    private final int price;

    @Schema(description = "생성 시간", example = "2026-07-08T15:12:45.51623")
    private final LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2026-07-08T15:13:30.677527")
    private final LocalDateTime updatedAt;

    public BookResponse(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.price = book.getPrice();
        this.createdAt = book.getCreatedAt();
        this.updatedAt = book.getUpdatedAt();
    }
}
