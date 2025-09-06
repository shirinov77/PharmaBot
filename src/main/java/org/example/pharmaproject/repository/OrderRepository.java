package org.example.pharmaproject.repository;

import org.example.pharmaproject.entities.Order;
import org.example.pharmaproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    long countByStatus(Order.Status status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o")
    double sumTotalPrices();
}