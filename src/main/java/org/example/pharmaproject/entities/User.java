package org.example.pharmaproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long telegramId;

    @NotBlank(message = "Ism bo‘sh bo‘lmasligi kerak")
    @Size(min = 2, max = 100, message = "Ism 2-100 ta belgi bo‘lishi kerak")
    private String name;

    @Size(max = 20, message = "Telefon raqami 20 belgidan oshmasligi kerak")
    private String phone;

    @Size(max = 255, message = "Manzil 255 belgidan oshmasligi kerak")
    private String address;

    @Column(length = 2, nullable = false)
    private String language = "uz";

    @Column
    private String state;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Basket basket;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
