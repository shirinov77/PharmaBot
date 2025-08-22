package org.example.pharmaproject.services;

import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.repository.BasketRepository;
import org.example.pharmaproject.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class BasketService {

    private final BasketRepository basketRepository;
    private final ProductRepository productRepository;

    public BasketService(BasketRepository basketRepository, ProductRepository productRepository) {
        this.basketRepository = basketRepository;
        this.productRepository = productRepository;
    }

    /**
     * Yangi bo‘sh savat yaratish (user bilan bog‘lanmagan)
     */
    @Transactional
    public Basket createBasket() {
        Basket basket = new Basket();
        basket.setProducts(new ArrayList<>());
        return basketRepository.save(basket);
    }

    /**
     * User uchun yangi basket yaratish va bog‘lash
     */
    @Transactional
    public Basket createBasketForUser(User user) {
        Basket basket = new Basket();
        basket.setProducts(new ArrayList<>());

        basket.setUser(user);
        user.setBasket(basket);

        return basketRepository.save(basket);
    }

    /**
     * Userning savatini olish (agar bo‘lmasa yangi yaratadi)
     */
    @Transactional
    public Basket getBasketByUser(User user) {
        return user.getBasket() != null
                ? basketRepository.findById(user.getBasket().getId())
                .orElseThrow(() -> new IllegalStateException("Savat topilmadi: " + user.getId()))
                : createBasketForUser(user);
    }

    /**
     * Savatga mahsulot qo‘shish
     */
    @Transactional
    public Product addToBasket(User user, Long productId) {
        Basket basket = getBasketByUser(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + productId));

        if (product.getQuantity() <= 0) {
            throw new IllegalStateException("Mahsulot omborda mavjud emas: " + product.getName());
        }

        if (!basket.getProducts().contains(product)) {
            basket.getProducts().add(product);
            basketRepository.save(basket);
        }

        // Mahsulot miqdorini kamaytirish
        product.setQuantity(product.getQuantity() - 1);
        productRepository.save(product);

        return product;
    }

    /**
     * Savatdan mahsulotni o‘chirish
     */
    @Transactional
    public void removeFromBasket(User user, Long productId) {
        Basket basket = getBasketByUser(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + productId));

        if (basket.getProducts().remove(product)) {
            basketRepository.save(basket);

            // Mahsulot miqdorini qaytarish
            product.setQuantity(product.getQuantity() + 1);
            productRepository.save(product);
        }
    }

    /**
     * Savatni tozalash
     */
    @Transactional
    public void clearBasket(User user) {
        Basket basket = getBasketByUser(user);

        // Copy list qilib olish kerak, bo‘lmasa ConcurrentModificationException bo‘ladi
        new ArrayList<>(basket.getProducts()).forEach(product -> {
            product.setQuantity(product.getQuantity() + 1);
            productRepository.save(product);
        });

        basket.getProducts().clear();
        basketRepository.save(basket);
    }

    /**
     * Savatni o‘chirish
     */
    @Transactional
    public void deleteBasket(Long basketId) {
        Basket basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new IllegalArgumentException("Savat topilmadi: " + basketId));
        basketRepository.delete(basket);
    }

    /**
     * Savat umumiy summasini hisoblash
     */
    @Transactional(readOnly = true)
    public double calculateTotal(Basket basket) {
        return basket.getProducts().stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }
}
