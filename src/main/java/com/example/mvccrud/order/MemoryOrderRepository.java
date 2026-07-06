package com.example.mvccrud.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//@Repository
public class MemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Order save(Order order) {
        Long id = ++sequence;

        Order savedOrder = new Order(
            id,
            order.getMember(),
            order.getBook(),
            order.getQuantity(),
            order.getOrderPrice()
        );

        store.put(id, savedOrder);
        return savedOrder;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Order> search(Long memberId, OrderStatus status) {
        return store.values().stream()
            .filter(order -> memberId == null || order.getMemberId().equals(memberId))
            .filter(order -> status == null || order.getStatus() == status)
            .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

}
