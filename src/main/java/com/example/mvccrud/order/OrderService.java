package com.example.mvccrud.order;

import com.example.mvccrud.book.Book;
import com.example.mvccrud.book.BookService;
import com.example.mvccrud.member.MemberService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
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

    public Order createOrder(Long memberId, Long bookId, int quantity) {
        memberService.findMember(memberId);
        Book book = bookService.findBook(bookId);

        Order order = new Order(
            null,
            memberId,
            bookId,
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

    public Order cancelOrder(Long id) {
        Order order = findOrder(id);
        order.cancel();
        return order;
    }

    public List<Order> searchOrders(Long memberId, OrderStatus status) {
        return orderRepository.search(memberId, status);
    }

}
