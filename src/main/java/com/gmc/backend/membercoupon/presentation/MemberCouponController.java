package com.gmc.backend.membercoupon.presentation;

import com.gmc.backend.common.response.ApiResponse;
import com.gmc.backend.membercoupon.application.MemberCouponService;
import com.gmc.backend.membercoupon.application.dto.MemberCouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-coupons")
@RequiredArgsConstructor
public class MemberCouponController {

    private final MemberCouponService memberCouponService;

    @PostMapping("/{couponId}")
    public ResponseEntity<ApiResponse<MemberCouponResponse>> saveCoupon(
            @PathVariable Long couponId,
            @AuthenticationPrincipal String email) {
        MemberCouponResponse response = memberCouponService.saveCoupon(email, couponId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<MemberCouponResponse>>> getMyCoupons(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.success(memberCouponService.getMyCoupons(email)));
    }
}
