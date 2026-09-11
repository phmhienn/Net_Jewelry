package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.enums.DomainEnums.OrderStatus;
import com.example.jewelrystore.exception.BadRequestException;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.service.StatisticsService;
import com.example.jewelrystore.util.Pages;
import java.time.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {
  private final StatisticsRepository repo;
  private final SanPhamRepository products;
  private final TaiKhoanRepository customers;

  private Instant[] period(LocalDate from, LocalDate to) {
    LocalDate end = to == null ? LocalDate.now(ZoneOffset.UTC) : to;
    LocalDate start = from == null ? end.withDayOfMonth(1) : from;
    require(!start.isAfter(end), "Khoảng ngày không hợp lệ");
    return new Instant[] {
      start.atStartOfDay(ZoneOffset.UTC).toInstant(),
      end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
    };
  }

  public DashboardResponse dashboard(LocalDate from, LocalDate to) {
    var dateRange = period(from, to);
    return new DashboardResponse(
        repo.countByDateGreaterThanEqualAndDateLessThan(dateRange[0], dateRange[1]),
        repo.countByStatusAndDateGreaterThanEqualAndDateLessThan(
            OrderStatus.HOAN_THANH, dateRange[0], dateRange[1]),
        repo.countByStatusAndDateGreaterThanEqualAndDateLessThan(
            OrderStatus.DA_HUY, dateRange[0], dateRange[1]),
        repo.countByStatusAndDateGreaterThanEqualAndDateLessThan(
            OrderStatus.DANG_GIAO_HANG, dateRange[0], dateRange[1]),
        products.count(),
        customers.countByAuthorityName("KHACH_HANG"),
        repo.revenue(OrderStatus.HOAN_THANH, dateRange[0], dateRange[1]));
  }

  public PageResponse<ReportRow> revenue(
      String group, LocalDate from, LocalDate to, boolean slow, int page, int size) {
    var dateRange = period(from, to);
    var paging = Pages.of(page, size, Sort.unsorted());
    return PageResponse.of(
        switch (group) {
          case "date" -> repo.byDate(OrderStatus.HOAN_THANH, dateRange[0], dateRange[1], paging);
          case "category" ->
              repo.byCategory(OrderStatus.HOAN_THANH, dateRange[0], dateRange[1], paging);
          case "product" ->
              repo.byProduct(OrderStatus.HOAN_THANH, dateRange[0], dateRange[1], slow, paging);
          default ->
              throw new BadRequestException("Nhóm thống kê phải là date, category hoặc product");
        });
  }

  public CustomerAnalyticsResponse customers(LocalDate from, LocalDate to, int page, int size) {
    var dateRange = period(from, to);
    return new CustomerAnalyticsResponse(
        repo.newCustomers(dateRange[0], dateRange[1]),
        repo.returningCustomers(OrderStatus.HOAN_THANH, dateRange[0], dateRange[1]),
        PageResponse.of(
            repo.topCustomers(
                OrderStatus.HOAN_THANH,
                dateRange[0],
                dateRange[1],
                Pages.of(page, size, Sort.unsorted()))));
  }
}
