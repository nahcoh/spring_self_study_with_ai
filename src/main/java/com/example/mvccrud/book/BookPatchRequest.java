package com.example.mvccrud.book;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookPatchRequest {

    @Schema(description = "부분 수정할 책 제목", example = "제목만 수정")
    private String title;
    @Schema(description = "부분 수정할 책 가격", example = "30000")
    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private Integer price;

    public BookPatchRequest(String title, Integer price) {
        this.title = title;
        this.price = price;
    }
}
