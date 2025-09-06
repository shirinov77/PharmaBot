package org.example.pharmaproject.services;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.repository.OrderRepository;
import org.example.pharmaproject.repository.ProductRepository;
import org.example.pharmaproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long getTotalOrders() {
        return orderRepository.count();
    }

    @Transactional(readOnly = true)
    public long getTotalProducts() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public double getTotalRevenue() {
        return orderRepository.sumTotalPrices();
    }
}