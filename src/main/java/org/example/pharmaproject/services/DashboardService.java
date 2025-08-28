package org.example.pharmaproject.services;

import org.example.pharmaproject.repository.OrderRepository;
import org.example.pharmaproject.repository.ProductRepository;
import org.example.pharmaproject.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public DashboardService(UserRepository userRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    public double getTotalRevenue() {
        return orderRepository.findAll()
                .stream()
                .mapToDouble(order -> order.getTotalPrice())
                .sum();
    }
}
