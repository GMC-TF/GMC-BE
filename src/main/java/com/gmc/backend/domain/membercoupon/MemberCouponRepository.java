package com.gmc.backend.domain.membercoupon;

import com.gmc.backend.domain.coupon.Coupon;
import com.gmc.backend.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    boolean existsByMemberAndCoupon(Member member, Coupon coupon);

    List<MemberCoupon> findAllByMember(Member member);

    List<MemberCoupon> findAllByCouponIn(List<Coupon> coupons);
}
