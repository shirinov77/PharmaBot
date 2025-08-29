package org.example.pharmaproject.repository;

import org.example.pharmaproject.entities.Category;
import org.example.pharmaproject.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    long count();
}
