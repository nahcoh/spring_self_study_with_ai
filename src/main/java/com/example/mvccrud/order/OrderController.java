package com.example.mvccrud.order;

import com.example.mvccrud.global.ApiResponse;
import com.example.mvccrud.global.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order API", description = "주문 생성, 조회, 취소, 검색 API")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "주문 생성", description = "회원 ID, 책 ID, 수량을 입력받아 주문을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
        @RequestBody @Valid OrderCreateRequest request
    ) {
        OrderResponse order = orderService.createOrderResponse(
            request.getMemberId(),
            request.getBookId(),
            request.getQuantity()
        );



        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.of(order));
    }


    @Operation(summary = "주문 단건 조회", description = "ID로 주문 한 건을 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> findOrder(@PathVariable Long id) {
        OrderResponse order = orderService.findOrderResponse(id);

        return ApiResponse.of(order);
    }

    @Operation(summary = "주문 목록 조회", description = "주문 목록을 페이징과 정렬 조건으로 조회합니다.")
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> findOrders(Pageable pageable) {
        Page<OrderResponse> orders = orderService.findOrderResponses(pageable);

        return ApiResponse.of(PageResponse.from(orders));
    }

    @Operation(summary = "주문 취소",description = "ID에 해당하는 주문을 취소 상태로 변경합니다.")
    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long id) {
        OrderResponse order = orderService.cancelOrderResponse(id);

        return ApiResponse.of(order);
    }

    @Operation(summary = "주문 검색", description = "회원 ID와 주문 상태 조건으로 주문을 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<PageResponse<OrderResponse>> searchOrders(
        @ModelAttribute OrderSearchRequest request, Pageable pageable) {
        Page<OrderResponse> orders = orderService.searchOrderResponses(
            request.getMemberId(),
            request.getStatus(),
            pageable
        );

        return ApiResponse.of(PageResponse.from(orders));
    }

}
