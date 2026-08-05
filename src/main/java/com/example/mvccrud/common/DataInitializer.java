package com.example.mvccrud.common;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.enabled;

import ch.qos.logback.core.joran.conditional.IfAction;
import com.example.mvccrud.book.Book;
import com.example.mvccrud.book.BookService;
import com.example.mvccrud.member.Member;
import com.example.mvccrud.member.MemberRepository;
import com.example.mvccrud.member.MemberService;
import com.example.mvccrud.order.Order;
import com.example.mvccrud.order.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev","mysql","docker"})
public class DataInitializer implements CommandLineRunner {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final BookService bookService;
    private final OrderService orderService;

    private final boolean adminInitializeEnabled;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;
    private final int adminAge;

    public DataInitializer(
        MemberService memberService,
        MemberRepository memberRepository,
        BookService bookService,
        OrderService orderService,
        @Value("${app.admin.initialize-enabled}") boolean adminInitializeEnabled,
        @Value("${app.admin.email}") String adminEmail,
        @Value("${app.admin.password}") String adminPassword,
        @Value("${app.admin.name}") String adminName,
        @Value("${app.admin.age}") int adminAge
    ) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.bookService = bookService;
        this.orderService = orderService;
        this.adminInitializeEnabled = adminInitializeEnabled;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
        this.adminAge = adminAge;
    }


    @Override
    public void run(String... args) {
        boolean hasInitialData = !memberService.findMembers().isEmpty();

        createInitialAdmin();

        if (hasInitialData) {
            return;
        }

        Member member1 = memberService.createMember("김철수", "kim@test.com", "password1234", 30);
        Member member2 = memberService.createMember("이영희", "lee@test.com", "password1234", 25);
        Member member3 = memberService.createMember("박민수", "park@test.com", "password1234", 28);
        Member member4 = memberService.createMember("최지은", "choi@test.com", "password1234", 32);
        Member member5 = memberService.createMember("정현우", "jung@test.com", "password1234", 27);

        Book book1 = bookService.createBook("데미안", 15000);
        Book book2 = bookService.createBook("클린 코드", 33000);
        Book book3 = bookService.createBook("이펙티브 자바", 45000);
        Book book4 = bookService.createBook("객체지향의 사실과 오해", 22000);
        Book book5 = bookService.createBook("토비의 스프링", 40000);
        Book book6 = bookService.createBook("자바의 정석", 30000);
        Book book7 = bookService.createBook("도메인 주도 설계", 38000);
        Book book8 = bookService.createBook("리팩터링", 36000);
        Book book9 = bookService.createBook("스프링 부트와 JPA", 28000);
        Book book10 = bookService.createBook("HTTP 완벽 가이드", 42000);

        Order order1 = orderService.createOrder(member1.getId(), book1.getId(), 2);
        Order order2 = orderService.createOrder(member1.getId(), book2.getId(), 1);
        Order order3 = orderService.createOrder(member2.getId(), book3.getId(), 1);
        Order order4 = orderService.createOrder(member3.getId(), book4.getId(), 3);
        Order order5 = orderService.createOrder(member4.getId(), book5.getId(), 1);
        Order order6 = orderService.createOrder(member5.getId(), book6.getId(), 2);
        Order order7 = orderService.createOrder(member2.getId(), book7.getId(), 1);
        Order order8 = orderService.createOrder(member3.getId(), book8.getId(), 1);
        Order order9 = orderService.createOrder(member4.getId(), book9.getId(), 2);
        Order order10 = orderService.createOrder(member5.getId(), book10.getId(), 1);

        orderService.cancelOrder(order3.getId());
        orderService.cancelOrder(order7.getId());
    }

    private void createInitialAdmin() {
        if (!adminInitializeEnabled) {
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException(
                "관리자 초기 비밀번호가 설정되지 않음. ADMIN_PASSWORD 환경변수를 설정해라."
            );
        }

        if (memberRepository.existsByEmail(adminEmail)) {
            return;
        }

        memberService.createAdminMember(
            adminName,
            adminEmail,
            adminPassword,
            adminAge
        );

    }
}