package com.example.mvccrud.book;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookUpdateRequest {

    @Schema(description = "수정할 책 제목", example = "수정된 데미안")
    @NotBlank(message = "책 제목은 필수입니다.")
    private String title;

    @Schema(description = "수정할 책 가격", example = "20000")
    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private int price;

    public BookUpdateRequest(String title, int price) {
        this.title = title;
        this.price = price;
    }
}
