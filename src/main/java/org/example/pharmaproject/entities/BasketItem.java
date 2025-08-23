package org.example.pharmaproject.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "basket_items")
@Data
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Har bir BasketItem bitta savatga tegishli
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", nullable = false)
    private Basket basket;

    // Har bir BasketItem bitta mahsulotga tegishli
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Mahsulotning savatdagi miqdori
    private int quantity;
}