package com.example.mvccrud.order;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@Getter
public class Order {

    @Id
    @GeneratedValue
    private Long id;
    private Long memberId;
    private Long bookId;
    private int quantity;
    private int orderPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order(Long memberId, Long bookId, int quantity, int orderPrice) {
        validateMemberId(memberId);
        validateBookId(bookId);
        validateQuantity(quantity);
        validateOrderPrice(orderPrice);

        this.memberId = memberId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.status = OrderStatus.ORDERED;
    }

    Order(Long id, Long memberId, Long bookId, int quantity, int orderPrice) {
        validateMemberId(memberId);
        validateBookId(bookId);
        validateQuantity(quantity);
        validateOrderPrice(orderPrice);

        this.id = id;
        this.memberId = memberId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.status = OrderStatus.ORDERED;
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
    }
    private void validateBookId(Long bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("책 ID는 필수입니다.");
            }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
        throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
        }
    }

    private void validateOrderPrice(int orderPrice) {
        if (orderPrice <= 0) {
            throw new IllegalArgumentException("주문 가격은 1원 이상이어야 합니다.");
        }
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
