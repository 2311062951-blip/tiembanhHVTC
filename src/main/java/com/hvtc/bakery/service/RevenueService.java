package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.RevenueReportDto;
import java.time.LocalDate;

public interface RevenueService {
    RevenueReportDto getRevenueReport(LocalDate startDate, LocalDate endDate, String period);
}
