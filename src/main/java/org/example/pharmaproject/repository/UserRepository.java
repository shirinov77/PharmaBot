package org.example.pharmaproject.repository;

import org.example.pharmaproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelegramId(Long telegramId);

    @Query("SELECT u FROM User u WHERE SIZE(u.orders) > 0")
    List<User> findUsersWithOrders();

    Optional<User> findByPhone(String phone);
}
