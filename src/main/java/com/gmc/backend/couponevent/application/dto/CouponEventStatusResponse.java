package com.gmc.backend.couponevent.application.dto;

import com.gmc.backend.domain.coupon.Coupon;
import com.gmc.backend.domain.couponevent.CouponEvent;
import com.gmc.backend.domain.membercoupon.MemberCoupon;

import java.time.LocalDateTime;
import java.util.List;

public record CouponEventStatusResponse(
        Long id,
        String name,
        String description,
        LocalDateTime startAt,
        int totalCount,
        int claimedCount,
        int remainingCount,
        List<CouponClaimInfo> claims
) {
    public record CouponClaimInfo(
            String memberName,
            String memberEmail,
            LocalDateTime savedAt
    ) {
        public static CouponClaimInfo from(MemberCoupon memberCoupon) {
            return new CouponClaimInfo(
                    memberCoupon.getMember().getName(),
                    memberCoupon.getMember().getEmail(),
                    memberCoupon.getSavedAt()
            );
        }
    }

    public static CouponEventStatusResponse from(CouponEvent event, List<Coupon> coupons, List<MemberCoupon> claims) {
        return new CouponEventStatusResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartAt(),
                coupons.size(),
                claims.size(),
                coupons.size() - claims.size(),
                claims.stream().map(CouponClaimInfo::from).toList()
        );
    }
}