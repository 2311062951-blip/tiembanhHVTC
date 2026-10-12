package com.hvtc.bakery.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSalesDto {
    private Long productId;
    private String productName;
    private String image;
    private String categoryName;
    private long totalQuantity;
    private BigDecimal totalRevenue;
}
