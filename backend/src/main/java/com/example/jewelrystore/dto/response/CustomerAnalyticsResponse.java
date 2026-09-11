package com.example.jewelrystore.dto.response;

public record CustomerAnalyticsResponse(
    long newCustomers, long returningCustomers, PageResponse<ReportRow> topCustomers) {}
