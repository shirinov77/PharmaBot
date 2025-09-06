package org.example.pharmaproject.repository;

import org.example.pharmaproject.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}