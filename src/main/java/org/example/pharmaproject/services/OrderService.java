package org.example.pharmaproject.services;

import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Order;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BasketService basketService;

    @Transactional
    public Order createOrderFromBasket(User user) {
        Basket basket = basketService.getBasketByUser(user);
        if (basket.getProducts().isEmpty()) {
            throw new IllegalStateException("Savat bo‘sh, buyurtma yaratib bo‘lmaydi.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setProducts(basket.getProducts());
        order.setTotalPrice(calculateTotalPrice(basket));
        order.setStatus(Order.Status.valueOf("PENDING"));
        order.setCreatedAt(LocalDateTime.now());

        basketService.clearBasket(user);

        return orderRepository.save(order);
    }

    @Transactional
    public void updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Buyurtma topilmadi: " + orderId));
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Noto‘g‘ri status: " + status);
        }
        order.setStatus(Order.Status.valueOf(status));
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> findOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Buyurtma topilmadi: " + orderId));
    }

    private double calculateTotalPrice(Basket basket) {
        return basket.getProducts().stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }

    private boolean isValidStatus(String status) {
        return List.of("PENDING", "CONFIRMED", "DELIVERED", "CANCELLED").contains(status);
    }
}
