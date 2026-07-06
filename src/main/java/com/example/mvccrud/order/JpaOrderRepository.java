package com.example.mvccrud.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepository {

    @Query("""
select o from Order o
where (:memberId is null or o.memberId =:memberId)
and (:status is null or o.status =:status)
""")
    List<Order> search(
        @Param("memberId") Long memberId,
        @Param("status") OrderStatus status
    );

}
