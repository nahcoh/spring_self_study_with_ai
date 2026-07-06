package com.example.mvccrud.order;

import com.example.mvccrud.book.Book;
import com.example.mvccrud.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private int quantity;
    private int orderPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order(Member member, Book book, int quantity, int orderPrice) {
        validateMember(member);
        validateBook(book);
        validateQuantity(quantity);
        validateOrderPrice(orderPrice);

        this.member = member;
        this.book = book;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.status = OrderStatus.ORDERED;
    }

    Order(Long id, Member member, Book book, int quantity, int orderPrice) {
        validateMember(member);
        validateBook(book);
        validateQuantity(quantity);
        validateOrderPrice(orderPrice);

        this.id = id;
        this.member = member;
        this.book = book;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.status = OrderStatus.ORDERED;
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("회원은 필수입니다.");
        }
    }
    private void validateBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("책은 필수입니다.");
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

    public Long getMemberId() {
        return member.getId();
    }

    public Long getBookId() {
        return book.getId();
    }

}
