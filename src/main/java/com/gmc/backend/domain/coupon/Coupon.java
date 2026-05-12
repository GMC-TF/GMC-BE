package com.gmc.backend.domain.coupon;

import com.gmc.backend.domain.couponevent.CouponEvent;
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

    @ManyToOne(fetch = FetchType.LAZY)
    private CouponEvent couponEvent;

    @Builder
    public Coupon(String name, String description, String imageKey, LocalDate expiresAt, CouponEvent couponEvent) {
        this.name = name;
        this.description = description;
        this.imageKey = imageKey;
        this.expiresAt = expiresAt;
        this.couponEvent = couponEvent;
    }

    public void update(String name, String description, String imageKey) {
        this.name = name;
        this.description = description;
        this.imageKey = imageKey;
    }
}
