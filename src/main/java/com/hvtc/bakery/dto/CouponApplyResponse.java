package com.hvtc.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponApplyResponse {
    private boolean success;
    private String message;
    private String code;
    private Integer discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
}
