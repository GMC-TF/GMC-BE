package com.gmc.backend.coupon.application.dto;

import com.gmc.backend.domain.coupon.Coupon;

import java.time.LocalDate;

public record CouponResponse(
        Long id,
        String name,
        String description,
        String imageKey,
        LocalDate expiresAt
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDescription(),
                coupon.getImageKey(),
                coupon.getExpiresAt()
        );
    }
}
