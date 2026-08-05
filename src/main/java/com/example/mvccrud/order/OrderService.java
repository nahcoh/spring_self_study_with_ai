package com.example.mvccrud.order;

import com.example.mvccrud.book.Book;
import com.example.mvccrud.book.BookService;
import com.example.mvccrud.global.ForbiddenException;
import com.example.mvccrud.member.Member;
import com.example.mvccrud.member.MemberService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberService memberService;
    private final BookService bookService;

    public OrderService(OrderRepository orderRepository, MemberService memberService,
        BookService bookService) {

        this.orderRepository = orderRepository;
        this.memberService = memberService;
        this.bookService = bookService;
    }

    @Transactional
    public Order createOrder(Long memberId, Long bookId, int quantity) {
        Member member = memberService.findMember(memberId);
        Book book = bookService.findBook(bookId);

        Order order = new Order(
            member,
            book,
            quantity,
            book.getPrice()
        );
        return orderRepository.save(order);
    }

    public Order findOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(OrderNotFoundException::new);
    }

    public List<Order> findOrders() {
        return orderRepository.findAll();
    }

    public Page<Order> findOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional
    public Order cancelOrder(Long id) {
        Order order = findOrder(id);
        order.cancel();
        return order;
    }

    public List<Order> searchOrders(Long memberId, OrderStatus status) {
        return orderRepository.search(memberId, status);
    }

    public Page<Order> searchOrders(Long memberId, OrderStatus status, Pageable pageable) {
        return orderRepository.search(memberId, status, pageable);
    }

    @Transactional
    public OrderResponse createOrderResponse(Long memberId, Long bookId, int quantity) {
        Order order = createOrder(memberId, bookId, quantity);
        return new OrderResponse(order);
    }

    public OrderResponse findOrderResponse(Long id) {
        Order order = findOrder(id);
        return new OrderResponse(order);
    }

    public Page<OrderResponse> findOrderResponses(Pageable pageable) {
        return orderRepository.findAll(pageable)
            .map(OrderResponse::new);
    }

    @Transactional
    public OrderResponse cancelOrderResponse(Long id) {
        Order order = cancelOrder(id);
        return new OrderResponse(order);
    }

    public Page<OrderResponse> searchOrderResponses(
        Long memberId,
        OrderStatus status,
        Pageable pageable
    ) {
        return orderRepository.search(memberId, status, pageable)
            .map(OrderResponse::new);
    }

    @Transactional
    public Order cancelMyOrder(Long orderId, Long memberId) {
        Order order = findOrder(orderId);

        if (!order.getMemberId().equals(memberId)) {
            throw new ForbiddenException("본인의 주문만 취소할 수 있습니다.");
        }
        order.cancel();
        return order;
    }

    public OrderResponse findMyOrderResponse(Long orderId, Long memberId) {
        Order order = findOrder(orderId);

        if (!order.getMemberId().equals(memberId)) {
            throw new ForbiddenException("본인의 주문만 조회할 수 있습니다.");
        }
        return new OrderResponse(order);
    }



}
