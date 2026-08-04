package com.example.mvccrud.admin;


import com.example.mvccrud.global.ApiResponse;
import com.example.mvccrud.global.PageResponse;
import com.example.mvccrud.member.MemberResponse;
import com.example.mvccrud.member.MemberService;
import com.example.mvccrud.order.OrderResponse;
import com.example.mvccrud.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final OrderService orderService;

    public AdminController(MemberService memberService, OrderService orderService) {
        this.memberService = memberService;
        this.orderService = orderService;
    }

    @GetMapping("/members")
    @Operation(summary = "관리자 회원 전체 조회", description = "관리자 권한으로 전체 회원 목록을 조회한다.")
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> findMembers(
        Pageable pageable) {
        Page<MemberResponse> members = memberService.findMemberResponses(pageable);

        return ResponseEntity.ok(
            new ApiResponse<>(PageResponse.from(members))
        );
    }


    @Operation(summary = "관리자 주문 전체 조회", description = "관리자 권한으로 전체 주문 목록을 조회한다.")
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> findOrders(Pageable pageable) {
        Page<OrderResponse> orders = orderService.findOrderResponses(pageable);

        return ResponseEntity.ok(
            new ApiResponse<>(PageResponse.from(orders))
        );
    }
}
