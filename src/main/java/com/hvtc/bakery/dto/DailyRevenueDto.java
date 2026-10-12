package com.hvtc.bakery.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRevenueDto {
    private LocalDate date;
    private String formattedDate;
    private BigDecimal revenue;
    private long orderCount;
    private BigDecimal avgOrderValue;
}
