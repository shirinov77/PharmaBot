package org.example.pharmaproject.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "users", indexes = @Index(columnList = "telegramId"))
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Telegram ID bo‘sh bo‘lmasligi kerak")
    @Column(unique = true, nullable = false)
    private Long telegramId;

    @NotBlank(message = "Ism bo‘sh bo‘lmasligi kerak")
    @Size(min = 2, max = 100, message = "Ism 2-100 ta belgi bo‘lishi kerak")
    @Column
    private String name;

    @Size(max = 20, message = "Telefon raqami 20 belgidan oshmasligi kerak")
    @Column
    private String phone;

    @Size(max = 255, message = "Manzil 255 belgidan oshmasligi kerak")
    @Column
    private String address;

    @NotBlank(message = "Til bo‘sh bo‘lmasligi kerak")
    @Size(min = 2, max = 2, message = "Til kodi 2 belgi bo‘lishi kerak")
    @Column(length = 2, nullable = false)
    private String language = "uz";

    @Column
    private String state;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Basket basket;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}