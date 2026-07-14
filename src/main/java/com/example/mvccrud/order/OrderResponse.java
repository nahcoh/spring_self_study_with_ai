package com.example.mvccrud.order;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class OrderResponse {

    @Schema(description = "주문 ID", example = "1")
    private final Long id;

    @Schema(description = "회원 ID", example = "1")
    private final Long memberId;

    @Schema(description = "회원 이름", example = "김철수")
    private final String memberName;

    @Schema(description = "책 ID", example = "1")
    private final Long bookId;

    @Schema(description = "책 제목", example = "데미안")
    private final String bookTitle;

    @Schema(description = "주문 수량", example = "2")
    private final int quantity;

    @Schema(description = "주문 당시 책 가격", example = "15000")
    private final int orderPrice;

    @Schema(description = "총 주문 금액", example = "30000")
    private final int totalPrice;

    @Schema(description = "주문 상태", example = "ORDERED")
    private final OrderStatus status;

    @Schema(description = "생성 시간", example = "2026-07-08T15:12:45.51623")
    private final LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2026-07-08T15:13:30.677527")
    private final LocalDateTime updatedAt;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.memberId = order.getMemberId();
        this.memberName = order.getMember().getName();
        this.bookId = order.getBookId();
        this.bookTitle = order.getBook().getTitle();
        this.quantity = order.getQuantity();
        this.orderPrice = order.getOrderPrice();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();

    }

}
