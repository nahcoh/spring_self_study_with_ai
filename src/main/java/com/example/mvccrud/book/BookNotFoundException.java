package com.example.mvccrud.book;

public class BookNotFoundException extends RuntimeException{

    public BookNotFoundException() {
        super("책을 찾을 수 없습니다.");
    }

    public BookNotFoundException(String message) {
        super(message);
    }
}
