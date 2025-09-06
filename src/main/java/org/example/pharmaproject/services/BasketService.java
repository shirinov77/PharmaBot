package org.example.pharmaproject.services;

import lombok.RequiredArgsConstructor;
import org.example.pharmaproject.entities.Basket;
import org.example.pharmaproject.entities.Product;
import org.example.pharmaproject.entities.User;
import org.example.pharmaproject.repository.BasketRepository;
import org.example.pharmaproject.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketRepository basketRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Basket createBasketForUser(User user) {
        Basket basket = new Basket();
        basket.setUser(user);
        basket.setProducts(new ArrayList<>());
        return basketRepository.save(basket);
    }

    @Transactional
    public Basket getBasketByUser(User user) {
        return user.getBasket() != null ? user.getBasket() : createBasketForUser(user);
    }

    @Transactional
    public void addOrIncreaseProductInBasket(User user, Product product) {
        Basket basket = getBasketByUser(user);

        Product existingProduct = basket.getProducts().stream()
                .filter(p -> p.getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingProduct != null) {
            existingProduct.setQuantityInBasket(existingProduct.getQuantityInBasket() + 1);
        } else {
            Product newProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + product.getId()));
            newProduct.setQuantityInBasket(1);
            basket.getProducts().add(newProduct);
        }

        basketRepository.save(basket);
    }

    @Transactional
    public void increaseProductQuantityInBasket(User user, Long productId) {
        Basket basket = getBasketByUser(user);
        Product productInBasket = findProductInBasket(basket, productId);
        productInBasket.setQuantityInBasket(productInBasket.getQuantityInBasket() + 1);
        basketRepository.save(basket);
    }

    @Transactional
    public void decreaseProductQuantityInBasket(User user, Long productId) {
        Basket basket = getBasketByUser(user);
        Product productInBasket = findProductInBasket(basket, productId);

        int currentQuantity = productInBasket.getQuantityInBasket();
        if (currentQuantity > 1) {
            productInBasket.setQuantityInBasket(currentQuantity - 1);
            basketRepository.save(basket);
        } else {
            removeProductFromBasket(user, productId);
        }
    }

    @Transactional
    public void removeProductFromBasket(User user, Long productId) {
        Basket basket = getBasketByUser(user);
        basket.getProducts().removeIf(product -> product.getId().equals(productId));
        basketRepository.save(basket);
    }

    @Transactional
    public void clearBasket(User user) {
        Basket basket = getBasketByUser(user);
        basket.getProducts().clear();
        basketRepository.save(basket);
    }

    @Transactional
    public void deleteBasket(Long basketId) {
        basketRepository.deleteById(basketId);
    }

    @Transactional(readOnly = true)
    public double calculateTotal(Basket basket) {
        return basket.getProducts().stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantityInBasket())
                .sum();
    }

    private Product findProductInBasket(Basket basket, Long productId) {
        return basket.getProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot savatda topilmadi: " + productId));
    }
}