package org.example.pharmaproject.services;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Order;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BasketService basketService;

    @Transactional(readOnly = true)
    public long countOrders() {
        return orderRepository.count();
    }

    @Transactional
    public Order createOrderFromBasket(User user) {
        Basket basket = basketService.getBasketByUser(user);
        if (basket.getProducts().isEmpty()) {
            throw new IllegalStateException("Savat bo‘sh, buyurtma yaratib bo‘lmaydi.");
        }

        Order order = new Order();
        order.setUser(user);

        List<Product> productsInOrder = new ArrayList<>();
        for (Product product : basket.getProducts()) {
            Product orderProduct = new Product();
            orderProduct.setId(product.getId());
            orderProduct.setName(product.getName());
            orderProduct.setPrice(product.getPrice());
            orderProduct.setQuantity(product.getQuantityInBasket());
            productsInOrder.add(orderProduct);
        }
        order.setProducts(productsInOrder);
        order.setTotalPrice(basketService.calculateTotal(basket));
        order.setStatus(Order.Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        basketService.clearBasket(user);

        return orderRepository.save(order);
    }

    @Transactional
    public void updateStatus(Long orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Buyurtma topilmadi: " + orderId));
        order.setStatus(status);
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

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long countNewOrders() {
        return orderRepository.count();
    }

    @Transactional(readOnly = true)
    public double successRate() {
        long total = orderRepository.count();
        if (total == 0) return 0.0;
        long delivered = orderRepository.countByStatus(Order.Status.DELIVERED);
        return (delivered * 100.0) / total;
    }
}