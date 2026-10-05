package com.fitkart.repository;

import com.fitkart.entity.Order;
import com.fitkart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findAllByOrderByOrderDateDesc();

    @Query("""
            SELECT o
            FROM Order o
            JOIN o.user u
            WHERE LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY o.orderDate DESC
            """)
    List<Order> searchOrders(@Param("search") String search);
}

