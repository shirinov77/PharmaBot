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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BasketService basketService;

    @Autowired
    public OrderService(OrderRepository orderRepository, BasketService basketService) {
        this.orderRepository = orderRepository;
        this.basketService = basketService;
    }

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
            Product newProduct = new Product();
            newProduct.setId(product.getId());
            newProduct.setName(product.getName());
            newProduct.setPrice(product.getPrice());
            newProduct.setQuantity(product.getQuantityInBasket());
            productsInOrder.add(newProduct);
        }
        order.setProducts(productsInOrder);
        order.setTotalPrice(basketService.calculateTotal(basket));
        order.setStatus(Order.Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        // Buyurtma yaratilgandan keyin savatni tozalash
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
    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
