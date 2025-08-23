package org.example.pharmaproject.services;

import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

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
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + id));
        productRepository.delete(product);
    }
}