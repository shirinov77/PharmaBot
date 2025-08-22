package org.example.pharmaproject.services;

import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.repository.BasketRepository;
import org.example.pharmaproject.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class BasketService {

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Basket createBasket() {
        Basket basket = new Basket();
        basket.setProducts(new ArrayList<>());
        return basketRepository.save(basket);
    }

    @Transactional
    public Basket getBasketByUser(User user) {
        return user.getBasket() != null
                ? basketRepository.findById(user.getBasket().getId())
                .orElseThrow(() -> new IllegalStateException("Savat topilmadi: " + user.getId()))
                : createBasketForUser(user);
    }

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

    @Transactional
    public void removeFromBasket(User user, Long productId) {
        Basket basket = getBasketByUser(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + productId));

        basket.getProducts().remove(product);
        basketRepository.save(basket);

        // Mahsulot miqdorini qaytarish
        product.setQuantity(product.getQuantity() + 1);
        productRepository.save(product);
    }

    @Transactional
    public void clearBasket(User user) {
        Basket basket = getBasketByUser(user);
        for (Product product : basket.getProducts()) {
            product.setQuantity(product.getQuantity() + 1);
            productRepository.save(product);
        }
        basket.getProducts().clear();
        basketRepository.save(basket);
    }

    @Transactional
    public void deleteBasket(Long basketId) {
        Basket basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new IllegalArgumentException("Savat topilmadi: " + basketId));
        basketRepository.delete(basket);
    }

    @Transactional
    public double calculateTotal(Basket basket) {
        return basket.getProducts().stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }

    private Basket createBasketForUser(User user) {
        Basket basket = createBasket();
        user.setBasket(basket);
        return basketRepository.save(basket);
    }
}
