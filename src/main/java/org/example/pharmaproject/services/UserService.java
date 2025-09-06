package org.example.pharmaproject.services;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BasketService basketService;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + id));
    }

    @Transactional(readOnly = true)
    public User findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi topilmadi: " + telegramId));
    }

    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            user.setCreatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(user);
            basketService.createBasketForUser(savedUser);
            return savedUser;
        } else {
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
    }

    @Transactional
    public User updateUserDetails(Long telegramId, String name, String phone, String address) {
        User user = findByTelegramId(telegramId);

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

    @Transactional
    public void updateLanguage(Long telegramId, String language) {
        User user = findByTelegramId(telegramId);
        user.setLanguage(language);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        if (user.getBasket() != null) {
            basketService.deleteBasket(user.getBasket().getId());
        }
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }
}