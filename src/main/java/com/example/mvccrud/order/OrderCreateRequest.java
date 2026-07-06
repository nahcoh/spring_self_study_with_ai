package com.example.mvccrud.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotNull(message = "책 ID는 필수입니다.")
    private Long bookId;

    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
    private int quantity;

    public OrderCreateRequest(Long memberId, Long bookId, int quantity) {
        this.memberId = memberId;
        this.bookId = bookId;
        this.quantity = quantity;
    }
}
