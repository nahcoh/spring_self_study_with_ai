package com.example.mvccrud.book;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookSearchRequest {

    @Schema(description = "검색할 책 제목", example = "자바")
    private String title;

    @Schema(description = "최소 가격", example = "10000")
    private Integer minPrice;

    @Schema(description = "최대 가격", example = "40000")
    private Integer maxPrice;



}
