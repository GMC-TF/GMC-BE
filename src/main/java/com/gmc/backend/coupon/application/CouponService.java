package com.gmc.backend.coupon.application;

import com.gmc.backend.coupon.application.dto.CouponResponse;
import com.gmc.backend.domain.coupon.Coupon;
import com.gmc.backend.domain.coupon.CouponRepository;
import com.gmc.backend.infra.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final S3Uploader s3Uploader;

    @Transactional(readOnly = true)
    public List<CouponResponse> getCoupons() {
        return couponRepository.findAll().stream()
                .map(CouponResponse::from)
                .toList();
    }

    @Transactional
    public CouponResponse createCoupon(String name, String description, LocalDate expiresAt, MultipartFile image) {
        String imageKey = s3Uploader.upload(image);
        Coupon coupon = Coupon.builder()
                .name(name)
                .description(description)
                .imageKey(imageKey)
                .expiresAt(expiresAt)
                .build();
        return CouponResponse.from(couponRepository.save(coupon));
    }
}
