package com.example.mvccrud.book;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BookResponse {

    private final Long id;
    private final String title;
    private final int price;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BookResponse(Long id, String title, int price, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public BookResponse(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.price = book.getPrice();
        this.createdAt = book.getCreatedAt();
        this.updatedAt = book.getUpdatedAt();

    }
}
