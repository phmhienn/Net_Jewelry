package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.StatisticsService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {
  private final StatisticsService service;

  @GetMapping("/dashboard")
  public ApiResponse<DashboardResponse> dashboard(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to) {
    return ApiResponse.ok(service.dashboard(from, to));
  }

  @GetMapping("/revenue")
  public ApiResponse<PageResponse<ReportRow>> revenue(
      @RequestParam(defaultValue = "date") String group,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @RequestParam(defaultValue = "false") boolean slow,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.revenue(group, from, to, slow, page, size));
  }

  @GetMapping("/customers")
  public ApiResponse<CustomerAnalyticsResponse> customers(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.customers(from, to, page, size));
  }
}
