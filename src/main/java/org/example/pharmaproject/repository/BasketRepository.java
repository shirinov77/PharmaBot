package org.example.pharmaproject.repository;

import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BasketRepository extends JpaRepository<Basket, Long> {

    Optional<Basket> findByUser(User user);

    @Query("SELECT b FROM Basket b WHERE b.products IS NOT EMPTY")
    Basket findFirstBasketWithProducts();
}