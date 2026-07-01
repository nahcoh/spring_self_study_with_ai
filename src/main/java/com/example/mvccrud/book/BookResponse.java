package com.example.mvccrud.book;

import lombok.Getter;

@Getter
public class BookResponse {

    private final Long id;
    private final String title;
    private final int price;

    public BookResponse(Long id, String title, int price) {
        this.id = id;
        this.title = title;
        this.price = price;
    }

    public BookResponse(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.price = book.getPrice();
    }
}
