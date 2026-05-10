package com.gmc.backend.membercoupon.application;

import com.gmc.backend.common.exception.CustomException;
import com.gmc.backend.common.exception.ErrorCode;
import com.gmc.backend.domain.coupon.Coupon;
import com.gmc.backend.domain.coupon.CouponRepository;
import com.gmc.backend.domain.member.Member;
import com.gmc.backend.domain.member.MemberRepository;
import com.gmc.backend.domain.membercoupon.MemberCoupon;
import com.gmc.backend.domain.membercoupon.MemberCouponRepository;
import com.gmc.backend.membercoupon.application.dto.MemberCouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberCouponService {

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public MemberCouponResponse saveCoupon(String email, Long couponId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

        if (memberCouponRepository.existsByMemberAndCoupon(member, coupon)) {
            throw new CustomException(ErrorCode.COUPON_ALREADY_SAVED);
        }

        MemberCoupon memberCoupon = MemberCoupon.builder()
                .member(member)
                .coupon(coupon)
                .build();
        return MemberCouponResponse.from(memberCouponRepository.save(memberCoupon));
    }

    @Transactional(readOnly = true)
    public List<MemberCouponResponse> getMyCoupons(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return memberCouponRepository.findAllByMember(member).stream()
                .map(MemberCouponResponse::from)
                .toList();
    }
}
