package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.response.*;
import java.time.LocalDate;

public interface StatisticsService {
  DashboardResponse dashboard(LocalDate from, LocalDate to);

  PageResponse<ReportRow> revenue(
      String group, LocalDate from, LocalDate to, boolean slow, int page, int size);

  CustomerAnalyticsResponse customers(LocalDate from, LocalDate to, int page, int size);
}
