package org.example.pharmaproject.services;

import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BasketService basketService;

    @Autowired
    public UserService(UserRepository userRepository, BasketService basketService) {
        this.userRepository = userRepository;
        this.basketService = basketService;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    /**
     * Yangi foydalanuvchini saqlash yoki mavjudini yangilash.
     * Yangi foydalanuvchi uchun avtomatik savat yaratadi.
     */
    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            // Yangi foydalanuvchi
            user.setCreatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(user);

            // Foydalanuvchi uchun savat yaratish
            Basket basket = basketService.createBasketForUser(savedUser);
            savedUser.setBasket(basket);

            return userRepository.save(savedUser);
        } else {
            // Mavjud foydalanuvchini yangilash
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
    }

    /**
     * Foydalanuvchi ma'lumotlarini yangilash.
     * Faqat o'zgartirilgan maydonlar yangilanadi.
     */
    @Transactional
    public User updateUserDetails(Long telegramId, String name, String phone, String address) {
        User user = findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + telegramId));

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone);
        }
        if (address != null && !address.trim().isEmpty()) {
            user.setAddress(address);
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Foydalanuvchining tilini yangilash.
     */
    @Transactional
    public void updateLanguage(Long telegramId, String language) {
        User user = findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + telegramId));
        user.setLanguage(language);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Foydalanuvchini va uning savatini o'chirish.
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + id));

        // Bog'langan savatni o'chirish
        if (user.getBasket() != null) {
            basketService.deleteBasket(user.getBasket().getId());
        }
        userRepository.delete(user);
    }

}
