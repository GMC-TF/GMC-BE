package com.gmc.backend.membercoupon.application.dto;

import com.gmc.backend.coupon.application.dto.CouponResponse;
import com.gmc.backend.domain.membercoupon.MemberCoupon;

import java.time.LocalDateTime;

public record MemberCouponResponse(
        Long id,
        CouponResponse coupon,
        LocalDateTime savedAt
) {
    public static MemberCouponResponse from(MemberCoupon memberCoupon) {
        return new MemberCouponResponse(
                memberCoupon.getId(),
                CouponResponse.from(memberCoupon.getCoupon()),
                memberCoupon.getSavedAt()
        );
    }
}
