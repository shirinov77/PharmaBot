package org.example.pharmaproject.services;

import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.repository.BasketRepository;
import org.example.pharmaproject.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class BasketService {

    private final BasketRepository basketRepository;
    private final ProductRepository productRepository;

    public BasketService(BasketRepository basketRepository, ProductRepository productRepository) {
        this.basketRepository = basketRepository;
        this.productRepository = productRepository;
    }

    /**
     * User uchun yangi savat yaratish va bog‘lash
     */
    @Transactional
    public Basket createBasketForUser(User user) {
        Basket basket = new Basket();
        basket.setUser(user);
        return basketRepository.save(basket);
    }

    /**
     * Userning savatini olish (agar bo‘lmasa yangi yaratadi)
     */
    @Transactional
    public Basket getBasketByUser(User user) {
        return Optional.ofNullable(user.getBasket())
                .orElseGet(() -> createBasketForUser(user));
    }

    /**
     * Mahsulotni savatga qo'shish
     */
    @Transactional
    public void addProductToBasket(User user, Product product) {
        Basket basket = getBasketByUser(user);

        Optional<Product> existingProductInBasket = basket.getProducts().stream()
                .filter(p -> p.getId().equals(product.getId()))
                .findFirst();

        if (existingProductInBasket.isPresent()) {
            // Agar mahsulot savatda bor bo'lsa, miqdorini oshirish
            Product p = existingProductInBasket.get();
            p.setQuantityInBasket(p.getQuantityInBasket() + 1);
        } else {
            // Aks holda, yangi mahsulotni qo'shish
            product.setQuantityInBasket(1);
            basket.getProducts().add(product);
        }

        basketRepository.save(basket);
    }

    /**
     * Savatdagi mahsulot miqdorini oshirish
     */
    @Transactional
    public void increaseProductQuantity(User user, Long productId) {
        Basket basket = getBasketByUser(user);
        Product productInBasket = basket.getProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot savatda topilmadi"));

        productInBasket.setQuantityInBasket(productInBasket.getQuantityInBasket() + 1);
        basketRepository.save(basket);
    }

    /**
     * Savatdagi mahsulot miqdorini kamaytirish
     */
    @Transactional
    public void decreaseProductQuantity(User user, Long productId) {
        Basket basket = getBasketByUser(user);
        Product productInBasket = basket.getProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot savatda topilmadi"));

        int currentQuantity = productInBasket.getQuantityInBasket();
        if (currentQuantity > 1) {
            productInBasket.setQuantityInBasket(currentQuantity - 1);
            basketRepository.save(basket);
        } else {
            // Agar miqdor 1 bo'lsa, mahsulotni savatdan o'chirish
            removeProductFromBasket(user, productId);
        }
    }

    /**
     * Mahsulotni savatdan o‘chirish
     */
    @Transactional
    public void removeProductFromBasket(User user, Long productId) {
        Basket basket = getBasketByUser(user);
        basket.getProducts().removeIf(product -> product.getId().equals(productId));
        basketRepository.save(basket);
    }

    /**
     * Savatni tozalash
     */
    @Transactional
    public void clearBasket(User user) {
        Basket basket = getBasketByUser(user);
        basket.getProducts().clear();
        basketRepository.save(basket);
    }

    /**
     * Savatni o‘chirish
     */
    @Transactional
    public void deleteBasket(Long basketId) {
        basketRepository.deleteById(basketId);
    }

    /**
     * Savat umumiy summasini hisoblash
     */
    @Transactional(readOnly = true)
    public double calculateTotal(Basket basket) {
        return basket.getProducts().stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantityInBasket())
                .sum();
    }
}