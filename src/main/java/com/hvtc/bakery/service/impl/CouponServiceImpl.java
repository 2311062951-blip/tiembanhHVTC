package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.dto.CouponApplyResponse;
import com.hvtc.bakery.dto.CouponDto;
import com.hvtc.bakery.entity.Coupon;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.exception.ResourceNotFoundException;
import com.hvtc.bakery.repository.CouponRepository;
import com.hvtc.bakery.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> findAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon findById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon findByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Vui lòng nhập mã giảm giá");
        }
        return couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá: " + code));
    }

    @Override
    @Transactional
    public Coupon saveCoupon(CouponDto dto) {
        String codeFormatted = dto.getCode().trim().toUpperCase();
        if (couponRepository.existsByCodeIgnoreCase(codeFormatted)) {
            throw new BadRequestException("Mã giảm giá '" + codeFormatted + "' đã tồn tại trong hệ thống");
        }

        Coupon coupon = Coupon.builder()
                .code(codeFormatted)
                .discountPercent(dto.getDiscountPercent())
                .minOrderAmount(dto.getMinOrderAmount() != null ? dto.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(dto.getMaxDiscountAmount())
                .expirationDate(dto.getExpirationDate())
                .usageLimit(dto.getUsageLimit())
                .usedCount(0)
                .active(dto.isActive())
                .build();

        return couponRepository.save(coupon);
    }

    @Override
    @Transactional
    public Coupon updateCoupon(Long id, CouponDto dto) {
        Coupon coupon = findById(id);
        String codeFormatted = dto.getCode().trim().toUpperCase();

        if (!coupon.getCode().equalsIgnoreCase(codeFormatted) && couponRepository.existsByCodeIgnoreCase(codeFormatted)) {
            throw new BadRequestException("Mã giảm giá '" + codeFormatted + "' đã tồn tại trong hệ thống");
        }

        coupon.setCode(codeFormatted);
        coupon.setDiscountPercent(dto.getDiscountPercent());
        coupon.setMinOrderAmount(dto.getMinOrderAmount() != null ? dto.getMinOrderAmount() : BigDecimal.ZERO);
        coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        coupon.setExpirationDate(dto.getExpirationDate());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setActive(dto.isActive());

        return couponRepository.save(coupon);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = findById(id);
        couponRepository.delete(coupon);
    }

    @Override
    @Transactional
    public Coupon toggleStatus(Long id) {
        Coupon coupon = findById(id);
        coupon.setActive(!coupon.isActive());
        return couponRepository.save(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponApplyResponse validateAndApplyCoupon(String code, BigDecimal orderTotal) {
        if (code == null || code.isBlank()) {
            return CouponApplyResponse.builder()
                    .success(false)
                    .message("Vui lòng nhập mã giảm giá")
                    .build();
        }

        if (orderTotal == null) {
            orderTotal = BigDecimal.ZERO;
        }

        String codeFormatted = code.trim().toUpperCase();
        Coupon coupon = couponRepository.findByCodeIgnoreCase(codeFormatted).orElse(null);

        if (coupon == null) {
            return CouponApplyResponse.builder()
                    .success(false)
                    .message("Mã giảm giá '" + codeFormatted + "' không tồn tại!")
                    .build();
        }

        if (!coupon.isActive()) {
            return CouponApplyResponse.builder()
                    .success(false)
                    .message("Mã giảm giá '" + codeFormatted + "' hiện đang ngưng áp dụng!")
                    .build();
        }

        if (coupon.getExpirationDate() != null && LocalDateTime.now().isAfter(coupon.getExpirationDate())) {
            return CouponApplyResponse.builder()
                    .success(false)
                    .message("Mã giảm giá '" + codeFormatted + "' đã hết hạn sử dụng!")
                    .build();
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return CouponApplyResponse.builder()
                    .success(false)
                    .message("Mã giảm giá '" + codeFormatted + "' đã hết số lượt sử dụng!")
                    .build();
        }

        if (coupon.getMinOrderAmount() != null && orderTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            String minAmountStr = currencyFormat.format(coupon.getMinOrderAmount()) + "đ";
            return CouponApplyResponse.builder()
                    .success(false)
                    .message("Đơn hàng tối thiểu để áp dụng mã '" + codeFormatted + "' là " + minAmountStr)
                    .build();
        }

        // Tính số tiền giảm giá
        BigDecimal discount = orderTotal.multiply(BigDecimal.valueOf(coupon.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }

        BigDecimal finalTotal = orderTotal.subtract(discount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        return CouponApplyResponse.builder()
                .success(true)
                .message("Áp dụng mã giảm giá " + coupon.getDiscountPercent() + "% thành công!")
                .code(coupon.getCode())
                .discountPercent(coupon.getDiscountPercent())
                .discountAmount(discount)
                .finalTotal(finalTotal)
                .build();
    }
}
