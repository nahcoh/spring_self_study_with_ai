package com.example.mvccrud.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    Page<Order> findAll(Pageable pageable);

    List<Order> search(Long memberId, OrderStatus status);

    Page<Order> search(Long memberId, OrderStatus status, Pageable pageable);

    boolean existsById(Long id);


}
