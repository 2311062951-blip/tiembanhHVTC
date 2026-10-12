package com.hvtc.bakery.controller;

import com.hvtc.bakery.dto.RevenueReportDto;
import com.hvtc.bakery.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/revenue")
@RequiredArgsConstructor
public class AdminRevenueController {

    private final RevenueService revenueService;

    @GetMapping
    public String revenuePage(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "30days") String period,
            Model model
    ) {
        RevenueReportDto report = revenueService.getRevenueReport(startDate, endDate, period);
        model.addAttribute("report", report);
        model.addAttribute("activeAdminMenu", "revenue");
        return "admin/revenue/index";
    }

    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<RevenueReportDto> getRevenueData(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "30days") String period
    ) {
        RevenueReportDto report = revenueService.getRevenueReport(startDate, endDate, period);
        return ResponseEntity.ok(report);
    }
}
