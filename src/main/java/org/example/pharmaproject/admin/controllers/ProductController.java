package org.example.pharmaproject.admin.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        return "products/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/create";
    }

    @PostMapping("/create")
    public String createProduct(@ModelAttribute Product product) {
        productService.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + id));
        model.addAttribute("product", product);
        return "products/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product) {
        product.setId(id);
        productService.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/admin/products";
    }
}
