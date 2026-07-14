package com.example.mvccrud.order;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearchRequest {

    @Schema(description = "검색할 회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "검색할 주문 상태", example = "CANCELED")
    private OrderStatus status;

}
