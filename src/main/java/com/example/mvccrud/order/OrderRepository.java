package com.example.mvccrud.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    List<Order> search(Long memberId, OrderStatus status);

    boolean existsById(Long id);


}
