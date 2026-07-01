package com.example.mvccrud.book;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookPatchRequest {

    private String title;

    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private Integer price;

    public BookPatchRequest(String title, Integer price) {
        this.title = title;
        this.price = price;
    }
}
