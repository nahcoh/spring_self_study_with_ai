package com.example.mvccrud.order;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearchRequest {

    private Long memberId;
    private OrderStatus status;

}
