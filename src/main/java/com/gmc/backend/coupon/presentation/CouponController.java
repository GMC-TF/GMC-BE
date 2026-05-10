package com.gmc.backend.coupon.presentation;

import com.gmc.backend.common.response.ApiResponse;
import com.gmc.backend.coupon.application.CouponService;
import com.gmc.backend.coupon.application.dto.CouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getCoupons(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCoupons()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
            @RequestParam MultipartFile image,
            @AuthenticationPrincipal String email) {
        CouponResponse response = couponService.createCoupon(name, description, expiresAt, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
