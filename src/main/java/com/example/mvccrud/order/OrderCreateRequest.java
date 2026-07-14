package com.example.mvccrud.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    @Schema(description = "주문할 회원 ID", example = "1")
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @Schema(description = "주문할 책 ID", example = "1")
    @NotNull(message = "책 ID는 필수입니다.")
    private Long bookId;

    @Schema(description = "주문 수량", example = "2")
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
    private int quantity;

    public OrderCreateRequest(Long memberId, Long bookId, int quantity) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.quantity = quantity;
    }
}
