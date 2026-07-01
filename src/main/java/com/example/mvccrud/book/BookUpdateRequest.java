package com.example.mvccrud.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookUpdateRequest {

    @NotBlank(message = "책 제목은 필수입니다.")
    private String title;

    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private int price;

    public BookUpdateRequest(String title, int price) {
        this.title = title;
        this.price = price;
    }
}
