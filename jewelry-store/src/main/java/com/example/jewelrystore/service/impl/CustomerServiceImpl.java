package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.CustomerService;
import com.example.jewelrystore.util.Pages;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {
  private final TaiKhoanRepository customers;
  private final CustomerQueries queries;
  private final UserMapper userMapper;
  private final CurrentActor actor;

  private Long customerId(Long requested) {
    if (requested == null) return actor.customerId();
    require(actor.get().role() == Role.QUAN_LY, "Chỉ quản lý được truy cập khách hàng khác");
    require(
        get(customers, requested).getRole() == Role.KHACH_HANG, "Tài khoản không phải khách hàng");
    return requested;
  }

  @Transactional(readOnly = true)
  public PageResponse<UserResponse> customers(String keyword, int page, int size) {
    return PageResponse.of(
        customers
            .findAll(
                (root, criteriaQuery, cb) -> {
                  var customerRole = cb.equal(root.get("authority").get("name"), "KHACH_HANG");
                  if (keyword == null || keyword.isBlank()) return customerRole;
                  String k =
                      "%"
                          + keyword
                              .trim()
                              .toLowerCase(Locale.ROOT)
                              .replace("!", "!!")
                              .replace("%", "!%")
                              .replace("_", "!_")
                          + "%";
                  return cb.and(
                      customerRole,
                      cb.or(
                          cb.like(cb.lower(root.get("name")), k, '!'),
                          cb.like(cb.lower(root.get("email")), k, '!')));
                },
                Pages.of(page, size))
            .map(userMapper::user));
  }

  @Transactional(readOnly = true)
  public CustomerSummaryResponse customer(Long id) {
    customerId(id);
    return new CustomerSummaryResponse(
        userMapper.user(get(customers, id)),
        queries.countByCustomerId(id),
        queries.spending(id, OrderStatus.HOAN_THANH));
  }

  public UserResponse updateCustomer(Long id, CustomerUpdateRequest request) {
    TaiKhoan kh = lock(customers, id);
    require(kh.getRole() == Role.KHACH_HANG, "Tài khoản không phải khách hàng");
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    if (!kh.getEmail().equals(email)) {
      require(!customers.existsByEmail(email), "Email đã được sử dụng");
      kh.setEmail(email);
      kh.setTokenVersion(kh.getTokenVersion() + 1);
      kh.setResetTokenHash(null);
      kh.setResetTokenExpiresAt(null);
    }
    kh.setName(request.name());
    kh.setPhone(request.phone());
    return userMapper.user(kh);
  }

  public UserResponse customerStatus(Long id, AccountStatusRequest request) {
    TaiKhoan kh = lock(customers, id);
    require(kh.getRole() == Role.KHACH_HANG, "Tài khoản không phải khách hàng");
    if (kh.getStatus() != request.status()) {
      kh.setStatus(request.status());
      kh.setTokenVersion(kh.getTokenVersion() + 1);
      kh.setResetTokenHash(null);
      kh.setResetTokenExpiresAt(null);
    }
    return userMapper.user(kh);
  }
}
