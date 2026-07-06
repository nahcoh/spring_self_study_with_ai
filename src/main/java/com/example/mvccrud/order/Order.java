package com.example.mvccrud.order;

import lombok.Getter;

@Getter
public class Order {

    private final Long id;
    private final Long memberId;
    private final Long bookId;
    private final int quantity;
    private final int orderPrice;
    private OrderStatus status;

    public Order(Long id, Long memberId, Long bookId, int quantity, int orderPrice) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        if (bookId == null) {
            throw new IllegalArgumentException("책 ID는 필수입니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
        }
        if (orderPrice <= 0) {
            throw new IllegalArgumentException("주문 가격은 1원 이상이어야 합니다.");
        }

        this.id = id;
        this.memberId = memberId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.status = OrderStatus.ORDERED;
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        this.status = OrderStatus.CANCELED;
    }

    public int getTotalPrice() {
        return orderPrice * quantity;
    }

}
