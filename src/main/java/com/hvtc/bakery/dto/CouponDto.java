package com.hvtc.bakery.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDto {
    private Long id;

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Mã giảm giá chỉ được gồm chữ cái, số và dấu gạch dưới")
    private String code;

    @NotNull(message = "Phần trăm giảm giá không được để trống")
    @Min(value = 1, message = "Phần trăm giảm tối thiểu 1%")
    @Max(value = 100, message = "Phần trăm giảm tối đa 100%")
    private Integer discountPercent;

    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime expirationDate;

    private Integer usageLimit;
    
    @Builder.Default
    private boolean active = true;
}
