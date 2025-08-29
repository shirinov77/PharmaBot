package org.example.pharmaproject.admin.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.Category;
import org.example.pharmaproject.services.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 1️⃣ Barcha kategoriyalar ro'yxati
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "categories/list";
    }

    // 2️⃣ Yangi kategoriya qo‘shish formasi
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/create";
    }

    // 3️⃣ Yangi kategoriya saqlash
    @PostMapping("/create")
    public String createCategory(@ModelAttribute Category category, Model model) {
        try {
            categoryService.save(category);
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "categories/create";
        }
    }

    // 4️⃣ Kategoriyani tahrirlash formasi
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategoriya topilmadi: " + id));
        model.addAttribute("category", category);
        return "categories/edit";
    }

    // 5️⃣ Kategoriyani yangilash
    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category, Model model) {
        Category existingCategory = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategoriya topilmadi: " + id));
        existingCategory.setName(category.getName());

        try {
            categoryService.save(existingCategory);
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "categories/edit";
        }
    }

    // 6️⃣ Kategoriyani o‘chirish
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, Model model) {
        try {
            categoryService.delete(id);
            return "redirect:/admin/categories";
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "categories/list";
        }
    }
}
