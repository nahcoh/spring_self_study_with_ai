package com.example.mvccrud.book;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class BookPatchRequest {

    private String title;

    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private Integer price;

}
