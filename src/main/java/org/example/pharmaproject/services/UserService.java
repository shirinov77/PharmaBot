package org.example.pharmaproject.services;

import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public User save(User user) {
        if (user.getId() == null && user.getTelegramId() != null) {
            // Yangi foydalanuvchi uchun avtomatik savat yaratish
            User savedUser = userRepository.save(user);
            Basket basket = basketService.createBasketForUser(savedUser);
            savedUser.setBasket(basket);
            return userRepository.save(savedUser);
        }
        return userRepository.save(user);
    }

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

        return userRepository.save(user);
    }

    @Transactional
    public void updateLanguage(Long telegramId, String language) {
        User user = findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + telegramId));
        user.setLanguage(language);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + id));
        if (user.getBasket() != null) {
            basketService.deleteBasket(user.getBasket().getId());
        }
        userRepository.delete(user);
    }
}