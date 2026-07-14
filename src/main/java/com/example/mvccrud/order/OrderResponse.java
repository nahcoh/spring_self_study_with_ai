package com.example.mvccrud.order;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class OrderResponse {

    private final Long id;
    private final Long memberId;
    private final String memberName;
    private final Long bookId;
    private final String bookTitle;
    private final int quantity;
    private final int orderPrice;
    private final int totalPrice;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
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
