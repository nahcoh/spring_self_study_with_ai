package com.example.mvccrud.admin;


import com.example.mvccrud.global.ApiResponse;
import com.example.mvccrud.global.PageResponse;
import com.example.mvccrud.global.SortValidator;
import com.example.mvccrud.member.MemberResponse;
import com.example.mvccrud.member.MemberService;
import com.example.mvccrud.order.Order;
import com.example.mvccrud.order.OrderResponse;
import com.example.mvccrud.order.OrderService;
import com.example.mvccrud.order.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final OrderService orderService;
    private final SortValidator sortValidator;

    private static final Set<String> ALLOWED_MEMBER_SORT_FIELDS = Set.of(
        "id", "name", "email", "age", "role", "createdAt", "updatedAt"
    );

    private static final Set<String> ALLOWED_ORDER_SORT_FIELDS = Set.of(
        "id", "quantity", "orderPrice", "status", "createdAt", "updatedAt"
    );

    public AdminController(
        MemberService memberService,
        OrderService orderService,
        SortValidator sortValidator
    ) {
        this.memberService = memberService;
        this.orderService = orderService;
        this.sortValidator = sortValidator;
    }

    @Operation(summary = "관리자 회원 전체 조회", description = "관리자 권한으로 전체 회원 목록을 조회한다.")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> findMembers(
        Pageable pageable
    ) {
        sortValidator.validate(pageable, ALLOWED_MEMBER_SORT_FIELDS);

        Page<MemberResponse> members = memberService.findMemberResponses(pageable);

        return ResponseEntity.ok(
            new ApiResponse<>(PageResponse.from(members))
        );
    }



    @Operation(summary = "관리자 주문 전체 조회", description = "관리자 권한으로 전체 주문 목록을 조회한다.")
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> findOrders(
        Pageable pageable
    ) {
        sortValidator.validate(pageable, ALLOWED_ORDER_SORT_FIELDS);

        Page<OrderResponse> orders = orderService.findOrderResponses(pageable);

        return ResponseEntity.ok(
            new ApiResponse<>(PageResponse.from(orders))
        );
    }

    @Operation(summary = "관리자 주문 검색", description = "관리자 권한으로 전체 주문을 회원 ID와 주문상태 조건으로 검색한다.")
    @GetMapping("/orders/search")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> searchOrders(
        @RequestParam(required = false) Long memberId,
        @RequestParam(required = false) OrderStatus status,
        Pageable pageable
    ) {
        sortValidator.validate(pageable, ALLOWED_ORDER_SORT_FIELDS);

        Page<OrderResponse> orders = orderService.searchOrderResponses(
            memberId,
            status,
            pageable
        );

        return ResponseEntity.ok(
            new ApiResponse<>(PageResponse.from(orders))
        );
    }

    @Operation(summary = "관리자 주문 강제 취소", description = "관리자 권한으로 특정 주문을 취소한다.")
    @PatchMapping("/orders/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrderByAdmin(
        @PathVariable Long id
    ) {
        OrderResponse response = orderService.cancelOrderByAdminResponse(id);

        return ResponseEntity.ok(
            new ApiResponse<>(response)
        );
    }

}
