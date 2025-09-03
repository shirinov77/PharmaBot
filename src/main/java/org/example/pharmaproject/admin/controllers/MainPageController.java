package org.example.pharmaproject.admin.controllers;

import org.example.pharmaproject.services.UserService;
import org.example.pharmaproject.services.OrderService;
import org.example.pharmaproject.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class MainPageController {

    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;

    public MainPageController(UserService userService, OrderService orderService, ProductService productService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }

    // Asosiy sahifa
    @GetMapping("/main")
    public String mainPage(Model model) {
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalOrders", orderService.countOrders());
        model.addAttribute("totalProducts", productService.countProducts());
        model.addAttribute("successRate", orderService.successRate());

        model.addAttribute("newOrders", orderService.countNewOrders());
        model.addAttribute("outOfStock", productService.countOutOfStock());

        return "main";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/users")
    public String usersPage() {
        return "users/list";
    }

    @GetMapping("/products")
    public String productsPage() {
        return "products/list";
    }

    @GetMapping("/orders")
    public String ordersPage() {
        return "orders/list";
    }
}
