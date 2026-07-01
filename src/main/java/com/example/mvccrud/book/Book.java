package com.example.mvccrud.book;


import lombok.Getter;

@Getter
public class Book {

    private final Long id;
    private String title;
    private int price;

    public Book(Long id, String title, int price) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("책 제목은 필수 입니다.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }

        this.id = id;
        this.title = title;
        this.price = price;
    }

    public void changeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("책 제목은 필수입니다.");
        }
        this.title = title;
    }

    public void changePrice(int price) {
        if (price <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야합니다.");
        }
        this.price = price;
    }
}
