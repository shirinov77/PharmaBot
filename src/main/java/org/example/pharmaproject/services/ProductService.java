package org.example.pharmaproject.services;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Product save(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Mahsulot nomi bo‘sh bo‘lmasligi kerak");
        }
        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Mahsulot narxi musbat bo‘lishi kerak");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Mahsulot miqdori manfiy bo‘lmasligi kerak");
        }
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + id));
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public long countOutOfStock() {
        return productRepository.countByQuantity(0);
    }

    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }
}