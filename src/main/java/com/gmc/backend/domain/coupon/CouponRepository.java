package com.gmc.backend.domain.coupon;

import com.gmc.backend.domain.couponevent.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findAllByCouponEvent(CouponEvent couponEvent);
}
