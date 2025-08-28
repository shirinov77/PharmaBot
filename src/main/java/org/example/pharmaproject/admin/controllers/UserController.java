package org.example.pharmaproject.admin.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/list";
    }

    @GetMapping("/{id}")
    public String userDetails(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + id));
        model.addAttribute("user", user);
        return "users/details";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}
