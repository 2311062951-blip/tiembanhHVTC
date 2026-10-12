package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.CouponApplyResponse;
import com.hvtc.bakery.dto.CouponDto;
import com.hvtc.bakery.entity.Coupon;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    List<Coupon> findAllCoupons();
    Coupon findById(Long id);
    Coupon findByCode(String code);
    Coupon saveCoupon(CouponDto dto);
    Coupon updateCoupon(Long id, CouponDto dto);
    void deleteCoupon(Long id);
    Coupon toggleStatus(Long id);
    CouponApplyResponse validateAndApplyCoupon(String code, BigDecimal orderTotal);
}
