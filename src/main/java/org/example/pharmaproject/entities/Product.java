package org.example.pharmaproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = @Index(columnList = "name"))
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mahsulot nomi bo‘sh bo‘lmasligi kerak")
    @Size(min = 2, max = 100, message = "Mahsulot nomi 2-100 ta belgi bo‘lishi kerak")
    @Column
    private String name;

    @NotNull(message = "Narx bo‘sh bo‘lmasligi kerak")
    @Positive(message = "Narx musbat bo‘lishi kerak")
    @Column
    private Double price;

    @NotNull(message = "Miqdor bo‘sh bo‘lmasligi kerak")
    @Positive(message = "Miqdor musbat bo‘lishi kerak")
    @Column
    private Integer quantity;

    @Size(max = 1000, message = "Tavsif 1000 belgidan oshmasligi kerak")
    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 500, message = "Rasm URL 500 belgidan oshmasligi kerak")
    @Column(length = 500)
    private String imageUrl;

    @ManyToMany
    @JoinTable(
            name = "product_related_products",
            joinColumns = @JoinColumn(name = "main_product_id"),
            inverseJoinColumns = @JoinColumn(name = "related_product_id")
    )
    private List<Product> relatedProducts = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}