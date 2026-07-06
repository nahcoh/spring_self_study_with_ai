package com.example.mvccrud.book;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Book {

    @Id
    @GeneratedValue
    private  Long id;

    private String title;
    private int price;

    public Book(String title, int price) {
        validatePrice(price);
        validateTitle(title);

        this.title = title;
        this.price = price;
    }

    public void changeTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    public void changePrice(int price) {
        validatePrice(price);
        this.price = price;

    }


    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("책 제목은 필수입니다.");
        }
    }

    private void validatePrice(int price) {
        if (price <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }

    }
}
