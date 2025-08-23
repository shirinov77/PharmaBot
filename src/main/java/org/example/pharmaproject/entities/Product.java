package org.example.pharmaproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mahsulot entity klassi.
 * Bu sinf ma'lumotlar bazasidagi 'products' jadvaliga mos keladi.
 */
@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(exclude = {"relatedProducts"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mahsulot nomi bo‘sh bo‘lmasligi kerak")
    @Size(min = 2, max = 100, message = "Mahsulot nomi 2-100 ta belgi bo‘lishi kerak")
    private String name;

    @NotNull(message = "Narx bo‘sh bo‘lmasligi kerak")
    @Positive(message = "Narx musbat bo‘lishi kerak")
    private Double price;

    @NotNull(message = "Miqdor bo‘sh bo‘lmasligi kerak")
    @PositiveOrZero(message = "Miqdor manfiy bo‘lmasligi kerak")
    private Integer quantity = 0;

    @Column(length = 1000)
    private String description; // Mahsulot tavsifi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 500)
    private String imageUrl;

    /**
     * Boshqa mahsulotlar bilan bog'lanish uchun many-to-many munosabati.
     * Qo'shimcha mahsulotlarni tavsiya qilish uchun ishlatiladi.
     */
    @ManyToMany
    @JoinTable(
            name = "product_related_products",
            joinColumns = @JoinColumn(name = "main_product_id"),
            inverseJoinColumns = @JoinColumn(name = "related_product_id")
    )
    private List<Product> relatedProducts = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    /**
     * Savatdagi miqdorni olish uchun yordamchi metod.
     * BasketItem orqali chaqiriladi.
     */
    @Transient
    private Integer quantityInBasket = 0;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Savatdagi miqdorni sozlash va olish
    public void setQuantityInBasket(int quantityInBasket) {
        this.quantityInBasket = quantityInBasket;
    }

    public int getQuantityInBasket() {
        return this.quantityInBasket != null ? this.quantityInBasket : 0;
    }
}
