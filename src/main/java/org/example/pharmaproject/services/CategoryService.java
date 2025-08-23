package org.example.pharmaproject.services;

import org.example.pharmaproject.entities.Category;
import org.example.pharmaproject.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Category save(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Kategoriya nomi bo‘sh bo‘lmasligi kerak");
        }
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        Category category = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategoriya topilmadi: " + id));
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("Kategoriyada mahsulotlar bor, o‘chirib bo‘lmaydi");
        }
        categoryRepository.deleteById(id);
    }
}