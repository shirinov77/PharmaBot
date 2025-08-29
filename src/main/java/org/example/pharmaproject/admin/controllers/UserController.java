package org.example.pharmaproject.admin.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1️⃣ Barcha foydalanuvchilar ro'yxati
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "users/list";
    }

    // 2️⃣ Foydalanuvchi tafsilotlari
    @GetMapping("/{id}")
    public String userDetails(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + id));
        model.addAttribute("user", user);
        return "users/details";
    }

    // 3️⃣ Foydalanuvchi ma'lumotlarini yangilash formasi
    @GetMapping("/edit/{telegramId}")
    public String editForm(@PathVariable Long telegramId, Model model) {
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + telegramId));
        model.addAttribute("user", user);
        return "users/edit";
    }

    // 4️⃣ Foydalanuvchi ma'lumotlarini yangilash
    @PostMapping("/edit/{telegramId}")
    public String updateUser(
            @PathVariable Long telegramId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address
    ) {
        userService.updateUserDetails(telegramId, name, phone, address);
        return "redirect:/admin/users/" + telegramId;
    }

    // 5️⃣ Foydalanuvchi tilini yangilash (REST)
    @PostMapping("/update-language/{telegramId}")
    @ResponseBody
    public String updateLanguage(
            @PathVariable Long telegramId,
            @RequestParam String language
    ) {
        userService.updateLanguage(telegramId, language);
        return "Foydalanuvchi tili yangilandi: " + language;
    }

    // 6️⃣ Foydalanuvchini o‘chirish
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}
