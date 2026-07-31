package com.example.mvccrud.order;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.mvccrud.book.Book;
import com.example.mvccrud.book.BookService;
import com.example.mvccrud.book.MemoryBookRepository;
import com.example.mvccrud.member.Member;
import com.example.mvccrud.member.MemberService;
import com.example.mvccrud.member.MemoryMemberRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class OrderServiceTest {

    private OrderService orderService;
    private MemberService memberService;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        MemoryMemberRepository memberRepository = new MemoryMemberRepository();
        MemoryBookRepository bookRepository = new MemoryBookRepository();
        MemoryOrderRepository orderRepository = new MemoryOrderRepository();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        memberService = new MemberService(memberRepository, passwordEncoder);
        bookService = new BookService(bookRepository);
        orderService = new OrderService(orderRepository, memberService, bookService);

    }

    @Test
    public void 주문_생성_성공() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        Book book = bookService.createBook("데미안", 15000);
        //when

        Order order = orderService.createOrder(member.getId(), book.getId(), 2);

        //then
        assertThat(order.getId()).isNotNull();
        assertThat(order.getMemberId()).isEqualTo(member.getId());
        assertThat(order.getBookId()).isEqualTo(book.getId());
        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getOrderPrice()).isEqualTo(15000);
        assertThat(order.getTotalPrice()).isEqualTo(30000);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDERED);
    }

    @Test
    public void 없는_회원으로_주문_생성_실패() throws Exception{
        //given
        Book book = bookService.createBook("데미안", 15000);
        //when//then
        assertThatThrownBy(() -> orderService.createOrder(999L, book.getId(), 2))
            .isInstanceOf(RuntimeException.class);

    }

    @Test
    public void 없는_책으로_주문_생성_실패() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        //when//then
        assertThatThrownBy(() -> orderService.createOrder(member.getId(), 999L, 2))
            .isInstanceOf(RuntimeException.class);

    }
    @Test
    public void 주문_단건_조회_성공() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        Book book = bookService.createBook("데미안", 15000);
        Order savedOrder = orderService.createOrder(member.getId(), book.getId(), 2);
        //when
        Order foundOrder = orderService.findOrder(savedOrder.getId());

        //then
        assertThat(foundOrder.getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.ORDERED);
    }

    @Test
    public void 없는_주문_조회_실패() throws Exception{
        //given

        //when//then
        assertThatThrownBy(() -> orderService.findOrder(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("주문을 찾을 수 없습니다.");
    }
    
    @Test
    public void 주문_전체_조회() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        Book book = bookService.createBook("데미안", 15000);
        Book book2 = bookService.createBook("자바의 정석", 30000);

        orderService.createOrder(member.getId(), book.getId(), 1);
        orderService.createOrder(member.getId(), book2.getId(), 2);
        //when
        List<Order> orders = orderService.findOrders();

        //then
        assertThat(orders).hasSize(2);
    }

    @Test
    public void 주문_취소_성공() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        Book book = bookService.createBook("데미안", 15000);
        Order order = orderService.createOrder(member.getId(), book.getId(), 2);

        //when
        Order canceledOrder = orderService.cancelOrder(order.getId());

        //then
        assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    public void 이미_취소된_주문_다시_취소_실패() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        Book book = bookService.createBook("데미안", 15000);
        Order order = orderService.createOrder(member.getId(), book.getId(), 2);
        orderService.cancelOrder(order.getId());

        //when//then
        assertThatThrownBy(() -> orderService.cancelOrder(order.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 취소된 주문입니다.");

    }

    @Test
    public void 회원ID로_주문_검색() throws Exception{
        //given
        Member member1 = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        Member member2 = memberService.createMember("이영희", "lee@test.com","password1234",25);
        Book book = bookService.createBook("데미안", 15000);

        orderService.createOrder(member1.getId(), book.getId(), 1);
        orderService.createOrder(member1.getId(), book.getId(), 2);
        orderService.createOrder(member2.getId(), book.getId(), 1);

        //when
        List<Order> orders = orderService.searchOrders(member1.getId(), null);

        //then
        assertThat(orders).hasSize(2);
    }

    @Test
    public void 상태로_주문_검색() throws Exception{
        //given
        Member member = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        Book book = bookService.createBook("데미안", 15000);

        Order order1 = orderService.createOrder(member.getId(), book.getId(), 1);
        orderService.createOrder(member.getId(), book.getId(), 2);

        orderService.cancelOrder(order1.getId());
        //when
        List<Order> canceledOrders = orderService.searchOrders(null, OrderStatus.CANCELED);
        List<Order> orderedOrders = orderService.searchOrders(null, OrderStatus.ORDERED);


        //then
        assertThat(orderedOrders).hasSize(1);
        assertThat(canceledOrders).hasSize(1);
    }

    @Test
    public void 회원ID와_상태로_주문_검색() throws Exception{
        //given
        Member member1 = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        Member member2 = memberService.createMember("이영희", "lee@test.com" ,"password1234",25);
        Book book = bookService.createBook("데미안", 15000);

        Order order1 = orderService.createOrder(member1.getId(), book.getId(), 1);
        orderService.createOrder(member1.getId(), book.getId(), 2);
        orderService.createOrder(member2.getId(), book.getId(), 1);

        orderService.cancelOrder(order1.getId());

        //when
        List<Order> orders = orderService.searchOrders(member1.getId(), OrderStatus.CANCELED);

        //then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getMemberId()).isEqualTo(member1.getId());
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

}