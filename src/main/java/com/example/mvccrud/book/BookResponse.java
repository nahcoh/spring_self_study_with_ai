package com.example.mvccrud.book;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;

public record BookResponse(

    @Schema(description = "책 ID", example = "1")
    Long id,

    @Schema(description = "책 제목", example = "데미안")
    String title,

    @Schema(description = "책 가격", example = "15000")
    int price,

    @Schema(description = "생성 시간", example = "2026-07-08T15:12:45.51623")
    LocalDateTime createdAt,

    @Schema(description = "수정 시간", example = "2026-07-08T15:13:30.677527")
    LocalDateTime updatedAt

    )implements Serializable {

    public BookResponse(Book book) {
        this(
            book.getId(),
            book.getTitle(),
            book.getPrice(),
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
    }
}
