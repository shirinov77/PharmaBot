package org.example.pharmaproject.admin.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.services.CategoryService;
import org.example.pharmaproject.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    // 1️⃣ Barcha mahsulotlar ro'yxati + qidiruv
    @GetMapping
    public String listProducts(Model model, @RequestParam(value = "search", required = false) String search) {
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchByName(search);
        } else {
            products = productService.findAll();
        }
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        return "products/list";
    }

    // 2️⃣ Yangi mahsulot qo‘shish formasi
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        return "products/create";
    }

    // 3️⃣ Yangi mahsulotni saqlash
    @PostMapping("/create")
    public String createProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        try {
            productService.save(product);
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Saqlashda xato: " + e.getMessage());
            return "redirect:/admin/products/create";
        }
    }

    // 4️⃣ Mahsulotni tahrirlash formasi
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Mahsulot topilmadi: " + id));
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.findAll());
            return "products/edit";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    // 5️⃣ Mahsulotni yangilash
    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        try {
            Product existingProduct = productService.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Mahsulot topilmadi: " + id));

            existingProduct.setName(product.getName());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setQuantity(product.getQuantity());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setCategory(product.getCategory());

            productService.save(existingProduct);
            return "redirect:/admin/products";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }

    // 6️⃣ Mahsulotni o‘chirish
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "O'chirishda xato: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }
}