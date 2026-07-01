package com.example.mvccrud.book;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookSearchRequest {

    private String title;
    private Integer minPrice;
    private Integer maxPrice;



}
