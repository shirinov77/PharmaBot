package org.example.pharmaproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
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
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
