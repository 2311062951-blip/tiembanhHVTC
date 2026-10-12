package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.dto.DailyRevenueDto;
import com.hvtc.bakery.dto.ProductSalesDto;
import com.hvtc.bakery.dto.RevenueReportDto;
import com.hvtc.bakery.entity.Order;
import com.hvtc.bakery.entity.OrderItem;
import com.hvtc.bakery.entity.OrderStatus;
import com.hvtc.bakery.entity.Product;
import com.hvtc.bakery.repository.OrderRepository;
import com.hvtc.bakery.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {

    private final OrderRepository orderRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public RevenueReportDto getRevenueReport(LocalDate startDate, LocalDate endDate, String period) {
        LocalDate today = LocalDate.now();

        // Handle pre-defined quick period presets
        if ("today".equalsIgnoreCase(period)) {
            startDate = today;
            endDate = today;
        } else if ("7days".equalsIgnoreCase(period)) {
            startDate = today.minusDays(6);
            endDate = today;
        } else if ("30days".equalsIgnoreCase(period) || (startDate == null && endDate == null)) {
            startDate = today.minusDays(29);
            endDate = today;
            period = "30days";
        } else if ("thisMonth".equalsIgnoreCase(period)) {
            startDate = today.withDayOfMonth(1);
            endDate = today;
        } else {
            // Custom date range fallback
            if (startDate == null) startDate = today.minusDays(29);
            if (endDate == null) endDate = today;
            if (startDate.isAfter(endDate)) {
                LocalDate temp = startDate;
                startDate = endDate;
                endDate = temp;
            }
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(startDateTime, endDateTime);

        // 1. Initialize empty map for all dates in range
        Map<LocalDate, List<Order>> ordersByDateMap = new LinkedHashMap<>();
        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            ordersByDateMap.put(curr, new ArrayList<>());
            curr = curr.plusDays(1);
        }

        // Group fetched orders into the dates map
        for (Order order : orders) {
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            if (ordersByDateMap.containsKey(orderDate)) {
                ordersByDateMap.get(orderDate).add(order);
            }
        }

        List<DailyRevenueDto> dailyRevenues = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long completedOrdersCount = 0;
        long cancelledOrdersCount = 0;
        long totalOrdersCount = orders.size();

        // Status distribution map
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusDistribution.put(status.getDisplayName(), 0L);
        }

        // Product sales aggregation map: ProductId -> ProductSalesDto
        Map<Long, ProductSalesDto> productSalesMap = new HashMap<>();

        for (Map.Entry<LocalDate, List<Order>> entry : ordersByDateMap.entrySet()) {
            LocalDate date = entry.getKey();
            List<Order> dayOrders = entry.getValue();

            BigDecimal dayRevenue = BigDecimal.ZERO;
            long dayOrderCount = dayOrders.size();

            for (Order order : dayOrders) {
                // Count status
                String statusName = order.getOrderStatus().getDisplayName();
                statusDistribution.put(statusName, statusDistribution.getOrDefault(statusName, 0L) + 1);

                if (order.getOrderStatus() == OrderStatus.CANCELLED) {
                    cancelledOrdersCount++;
                } else {
                    // Valid non-cancelled orders add to total revenue
                    BigDecimal grandTotal = order.getGrandTotal();
                    dayRevenue = dayRevenue.add(grandTotal);

                    if (order.getOrderStatus() == OrderStatus.COMPLETED) {
                        completedOrdersCount++;
                    }

                    // Aggregate product sales
                    if (order.getItems() != null) {
                        for (OrderItem item : order.getItems()) {
                            Product product = item.getProduct();
                            if (product != null) {
                                Long pid = product.getId();
                                ProductSalesDto sales = productSalesMap.computeIfAbsent(pid, k -> ProductSalesDto.builder()
                                        .productId(pid)
                                        .productName(product.getName())
                                        .image(product.getImage())
                                        .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Khác")
                                        .totalQuantity(0L)
                                        .totalRevenue(BigDecimal.ZERO)
                                        .build());

                                sales.setTotalQuantity(sales.getTotalQuantity() + item.getQuantity());
                                sales.setTotalRevenue(sales.getTotalRevenue().add(item.getSubtotal()));
                            }
                        }
                    }
                }
            }

            totalRevenue = totalRevenue.add(dayRevenue);
            BigDecimal avgDayOrder = dayOrderCount > 0 
                    ? dayRevenue.divide(BigDecimal.valueOf(dayOrderCount), 0, RoundingMode.HALF_UP) 
                    : BigDecimal.ZERO;

            dailyRevenues.add(DailyRevenueDto.builder()
                    .date(date)
                    .formattedDate(date.format(DATE_FORMATTER))
                    .revenue(dayRevenue)
                    .orderCount(dayOrderCount)
                    .avgOrderValue(avgDayOrder)
                    .build());
        }

        // Summary calculations
        long daysCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (daysCount <= 0) daysCount = 1;

        BigDecimal dailyAvgRevenue = totalRevenue.divide(BigDecimal.valueOf(daysCount), 0, RoundingMode.HALF_UP);
        long validOrdersCount = totalOrdersCount - cancelledOrdersCount;
        BigDecimal avgOrderValue = validOrdersCount > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(validOrdersCount), 0, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;

        // Top 10 Products by revenue
        List<ProductSalesDto> topProducts = productSalesMap.values().stream()
                .sorted(Comparator.comparing(ProductSalesDto::getTotalRevenue).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return RevenueReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .selectedPeriod(period)
                .totalRevenue(totalRevenue)
                .completedOrdersCount(completedOrdersCount)
                .totalOrdersCount(totalOrdersCount)
                .cancelledOrdersCount(cancelledOrdersCount)
                .averageOrderValue(avgOrderValue)
                .dailyAverageRevenue(dailyAvgRevenue)
                .dailyRevenues(dailyRevenues)
                .topProducts(topProducts)
                .statusDistribution(statusDistribution)
                .build();
    }
}
