package org.example.pharmaproject.admin.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.Order;
import org.example.pharmaproject.services.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Buyurtma topilmadi: " + id));
            model.addAttribute("order", order);
            return "orders/details";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam("status") Order.Status status, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, status);
            return "redirect:/admin/orders/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Statusni yangilashda xato: " + e.getMessage());
            return "redirect:/admin/orders/" + id;
        }
    }
}