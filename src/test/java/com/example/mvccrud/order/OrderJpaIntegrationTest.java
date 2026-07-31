package com.example.mvccrud.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.example.mvccrud.book.Book;
import com.example.mvccrud.book.BookService;
import com.example.mvccrud.member.Member;
import com.example.mvccrud.member.MemberService;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderJpaIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired MemberService memberService;
    @Autowired BookService bookService;
    @Autowired EntityManager em;

    @Test
    public void 주문_생성시_DB에_저장된다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@teest.com","password1234", 30);
        Book book = bookService.createBook("데미안", 15000);
        //when
        Order order = orderService.createOrder(member.getId(), book.getId(), 2);

        //then
        Order foundOrder = em.find(Order.class, order.getId());

        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getMemberId()).isEqualTo(member.getId());
        assertThat(foundOrder.getBookId()).isEqualTo(book.getId());
        assertThat(foundOrder.getQuantity()).isEqualTo(2);
        assertThat(foundOrder.getOrderPrice()).isEqualTo(15000);
        assertThat(foundOrder.getTotalPrice()).isEqualTo(30000);
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.ORDERED);
    }

    @Test
    public void 주문_조회가_된다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@teest.com", "password1234",30);
        Book book = bookService.createBook("데미안", 15000);
        Order order = orderService.createOrder(member.getId(), book.getId(), 2);
        //when
        Order foundOrder = orderService.findOrder(order.getId());
        //then
        assertThat(foundOrder.getId()).isEqualTo(order.getId());
        assertThat(foundOrder.getMemberId()).isEqualTo(member.getId());
        assertThat(foundOrder.getBookId()).isEqualTo(book.getId());
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.ORDERED);
    }

    @Test
    public void 없는_주문_조회_실패() throws Exception{
        //when//then
        assertThatThrownBy(() -> orderService.findOrder(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    public void 주문_취소시_상태가_CANCELED로_변경된다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@teest.com", "password1234",30);
        Book book = bookService.createBook("데미안", 15000);
        Order order = orderService.createOrder(member.getId(), book.getId(), 2);

        //when
        orderService.cancelOrder(order.getId());

        //then
        Order foundOrder = em.find(Order.class, order.getId());

        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    public void 취소된_주문을_다시_취소하면_실패한다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@teest.com", "password1234",30);
        Book book = bookService.createBook("데미안", 15000);
        Order order = orderService.createOrder(member.getId(), book.getId(), 2);
        //when
        orderService.cancelOrder(order.getId());

        //then
        assertThatThrownBy(() -> orderService.cancelOrder(order.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 취소된 주문입니다.");

    }

    @Test
    public void memberId로_주문_검색이_된다() throws Exception{
        //given
        Member member1 = memberService.createMember("김철수", "kim@teest.com", "password1234",30);
        Member member2 = memberService.createMember("이영희", "lee@test.com", "password", 20);

        Book book = bookService.createBook("데미안", 15000);

        orderService.createOrder(member1.getId(), book.getId(), 2);
        orderService.createOrder(member1.getId(), book.getId(), 1);
        orderService.createOrder(member2.getId(), book.getId(), 3);
        //when
        List<Order> orders = orderService.searchOrders(member1.getId(), null);

        //then
        assertThat(orders).hasSize(2);
        assertThat(orders)
            .extracting(Order::getMemberId)
            .containsOnly(member1.getId());
    }

    @Test
    public void status로_주문_검색이_된다() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@teest.com", "password1234",30);
        Book book = bookService.createBook("데미안", 15000);

        Order order1 = orderService.createOrder(member.getId(), book.getId(), 2);
        orderService.createOrder(member.getId(), book.getId(), 1);
        orderService.createOrder(member.getId(), book.getId(), 3);

        orderService.cancelOrder(order1.getId());
        //when
        List<Order> orders = orderService.searchOrders(null, OrderStatus.CANCELED);

        //then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    public void memberId와_status로_주문_검색이_된다() throws Exception{
        //given
        Member member1 = memberService.createMember("김철수", "kim@teest.com", "password1234",30);
        Member member2 = memberService.createMember("이영희", "lee@test.com", "password",20);
        Book book = bookService.createBook("데미안", 15000);

        Order order1 = orderService.createOrder(member1.getId(), book.getId(), 2);
        orderService.createOrder(member1.getId(), book.getId(), 1);

        Order order3 = orderService.createOrder(member2.getId(), book.getId(), 3);

        orderService.cancelOrder(order1.getId());
        orderService.cancelOrder(order3.getId());

        //when
        List<Order> orders = orderService.searchOrders(member1.getId(), OrderStatus.CANCELED);

        //then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getMemberId()).isEqualTo(member1.getId());
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.CANCELED);
    }
}