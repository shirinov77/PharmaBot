package org.example.pharmaproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "basket_items", indexes = @Index(columnList = "basket_id,product_id"))
@Getter
@Setter
@NoArgsConstructor
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", nullable = false)
    private Basket basket;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Positive(message = "Miqdor musbat bo‘lishi kerak")
    private int quantity;
}