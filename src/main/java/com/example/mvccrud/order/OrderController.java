package com.example.mvccrud.order;

import com.example.mvccrud.global.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
        Order order = orderService.createOrder(
            request.getMemberId(),
            request.getBookId(),
            request.getQuantity()
        );

        ApiResponse<OrderResponse> response = ApiResponse.of(
            new OrderResponse(order));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(response);
    }


    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> findOrder(@PathVariable Long id) {
        Order order = orderService.findOrder(id);
        return ApiResponse.of(new OrderResponse(order));
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> findOrders() {
        List<OrderResponse> orders = orderService.findOrders().stream()
            .map(OrderResponse::new)
            .toList();

        return ApiResponse.of(orders);
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long id) {
        Order order = orderService.cancelOrder(id);
        return ApiResponse.of(new OrderResponse(order));
    }

    @GetMapping("/search")
    public ApiResponse<List<OrderResponse>> searchOrders(
        @ModelAttribute OrderSearchRequest request) {
        List<OrderResponse> orders = orderService.searchOrders(
                request.getMemberId(),
                request.getStatus()
            ).stream()
            .map(OrderResponse::new)
            .toList();
        return ApiResponse.of(orders);
    }

}
