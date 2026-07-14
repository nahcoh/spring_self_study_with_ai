package com.example.mvccrud.order;

import com.example.mvccrud.global.ApiResponse;
import com.example.mvccrud.global.PageResponse;
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

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

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


    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> findOrder(@PathVariable Long id) {
        OrderResponse order = orderService.findOrderResponse(id);

        return ApiResponse.of(order);
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> findOrders(Pageable pageable) {
        Page<OrderResponse> orders = orderService.findOrderResponses(pageable);

        return ApiResponse.of(PageResponse.from(orders));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long id) {
        OrderResponse order = orderService.cancelOrderResponse(id);

        return ApiResponse.of(order);
    }

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
