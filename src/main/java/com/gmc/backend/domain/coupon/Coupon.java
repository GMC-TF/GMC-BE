package com.gmc.backend.domain.coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String imageKey;

    private LocalDate expiresAt;

    @Builder
    public Coupon(String name, String description, String imageKey, LocalDate expiresAt) {
        this.name = name;
        this.description = description;
        this.imageKey = imageKey;
        this.expiresAt = expiresAt;
    }
}
