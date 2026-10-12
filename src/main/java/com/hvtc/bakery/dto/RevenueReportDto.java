package com.hvtc.bakery.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String selectedPeriod;
    private BigDecimal totalRevenue;
    private long completedOrdersCount;
    private long totalOrdersCount;
    private long cancelledOrdersCount;
    private BigDecimal averageOrderValue;
    private BigDecimal dailyAverageRevenue;

    private List<DailyRevenueDto> dailyRevenues;
    private List<ProductSalesDto> topProducts;
    private Map<String, Long> statusDistribution;
}
