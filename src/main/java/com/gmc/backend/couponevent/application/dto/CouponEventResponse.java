package com.gmc.backend.couponevent.application.dto;

import com.gmc.backend.domain.couponevent.CouponEvent;

import java.time.LocalDateTime;

public record CouponEventResponse(
        Long id,
        String name,
        String description,
        LocalDateTime startAt,
        int couponCount
) {
    public static CouponEventResponse from(CouponEvent event, int couponCount) {
        return new CouponEventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartAt(),
                couponCount
        );
    }
}