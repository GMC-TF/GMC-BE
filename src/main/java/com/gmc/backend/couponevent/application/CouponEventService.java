package com.gmc.backend.couponevent.application;

import com.gmc.backend.common.exception.CustomException;
import com.gmc.backend.common.exception.ErrorCode;
import com.gmc.backend.couponevent.application.dto.CouponEventResponse;
import com.gmc.backend.couponevent.application.dto.CouponEventStatusResponse;
import com.gmc.backend.domain.coupon.Coupon;
import com.gmc.backend.domain.coupon.CouponRepository;
import com.gmc.backend.domain.couponevent.CouponEvent;
import com.gmc.backend.domain.couponevent.CouponEventRepository;
import com.gmc.backend.domain.membercoupon.MemberCoupon;
import com.gmc.backend.domain.membercoupon.MemberCouponRepository;
import com.gmc.backend.infra.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CouponEventService {

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public CouponEventResponse createEvent(String name, String description, LocalDateTime startAt,
                                           MultipartFile image, int quantity) {
        if (quantity < 1) {
            throw new CustomException(ErrorCode.INVALID_COUPON_QUANTITY);
        }

        String imageKey = s3Uploader.upload(image);

        CouponEvent event = couponEventRepository.save(CouponEvent.builder()
                .name(name)
                .description(description)
                .startAt(startAt)
                .build());

        List<Coupon> coupons = IntStream.range(0, quantity)
                .mapToObj(i -> Coupon.builder()
                        .name(name)
                        .description(description)
                        .imageKey(imageKey)
                        .couponEvent(event)
                        .build())
                .toList();
        couponRepository.saveAll(coupons);

        return CouponEventResponse.from(event, quantity);
    }

    @Transactional
    public CouponEventResponse updateEvent(Long eventId, String name, String description,
                                           LocalDateTime startAt, MultipartFile image) {
        CouponEvent event = couponEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_EVENT_NOT_FOUND));

        event.update(name, description, startAt);

        List<Coupon> coupons = couponRepository.findAllByCouponEvent(event);
        String imageKey = image != null ? s3Uploader.upload(image) : coupons.isEmpty() ? null : coupons.get(0).getImageKey();
        coupons.forEach(coupon -> coupon.update(name, description, imageKey));

        return CouponEventResponse.from(event, coupons.size());
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        CouponEvent event = couponEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_EVENT_NOT_FOUND));
        List<Coupon> coupons = couponRepository.findAllByCouponEvent(event);
        memberCouponRepository.deleteAll(memberCouponRepository.findAllByCouponIn(coupons));
        couponRepository.deleteAll(coupons);
        couponEventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<CouponEventStatusResponse> getAllEventStatus() {
        List<CouponEvent> events = couponEventRepository.findAll();
        return events.stream()
                .map(event -> {
                    List<Coupon> coupons = couponRepository.findAllByCouponEvent(event);
                    List<MemberCoupon> claims = memberCouponRepository.findAllByCouponIn(coupons);
                    return CouponEventStatusResponse.from(event, coupons, claims);
                })
                .toList();
    }
}