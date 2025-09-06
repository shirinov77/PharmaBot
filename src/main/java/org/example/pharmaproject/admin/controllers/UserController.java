package org.example.pharmaproject.admin.controllers;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

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
    public String userDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Foydalanuvchi topilmadi: " + id));
            model.addAttribute("user", user);
            return "users/details";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    // 3️⃣ Foydalanuvchi ma'lumotlarini yangilash formasi
    @GetMapping("/edit/{telegramId}")
    public String editForm(@PathVariable Long telegramId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByTelegramId(telegramId)
                    .orElseThrow(() -> new NoSuchElementException("Foydalanuvchi topilmadi: " + telegramId));
            model.addAttribute("user", user);
            return "users/edit";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    // 4️⃣ Foydalanuvchi ma'lumotlarini yangilash
    @PostMapping("/edit/{telegramId}")
    public String updateUser(
            @PathVariable Long telegramId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.updateUserDetails(telegramId, name, phone, address);
            return "redirect:/admin/users/" + telegramId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ma'lumotlarni yangilashda xato: " + e.getMessage());
            return "redirect:/admin/users/edit/" + telegramId;
        }
    }

    // 5️⃣ Foydalanuvchi tilini yangilash (REST)
    @PostMapping("/update-language/{telegramId}")
    public ResponseEntity<String> updateLanguage(
            @PathVariable Long telegramId,
            @RequestParam String language
    ) {
        try {
            userService.updateLanguage(telegramId, language);
            return ResponseEntity.ok("Foydalanuvchi tili yangilandi: " + language);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Xato: " + e.getMessage());
        }
    }

    // 6️⃣ Foydalanuvchini o‘chirish
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "O'chirishda xato: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
}