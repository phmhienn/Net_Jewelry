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
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final com.example.jewelrystore.service.RoleService roles;
  private final GioHangRepository carts;
  private final PasswordEncoder passwords;

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
                          cb.like(cb.lower(root.get("email")), k, '!'),
                          cb.like(cb.lower(root.get("phone")), k, '!')));
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

  public UserResponse createCustomer(CustomerCreateRequest request) {
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    require(!customers.existsByEmail(email), "Email đã được sử dụng");
    password(request.password());

    String username;
    do {
      username = "kh_" + UUID.randomUUID().toString().replace("-", "");
    } while (customers.existsByUsername(username));

    TaiKhoan kh = new TaiKhoan();
    kh.setName(request.name().trim());
    kh.setEmail(email);
    kh.setPhone(request.phone());
    kh.setUsername(username);
    kh.setPassword(passwords.encode(request.password()));
    kh.setStatus(request.status() == null ? AccountStatus.HOAT_DONG : request.status());
    kh.setAuthority(roles.get(Role.KHACH_HANG));
    customers.save(kh);

    GioHang cart = new GioHang();
    cart.setCustomer(kh);
    carts.save(cart);

    return userMapper.user(kh);
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

  public void deleteCustomer(Long id) {
    TaiKhoan kh = lock(customers, id);
    require(kh.getRole() == Role.KHACH_HANG, "Tài khoản không phải khách hàng");
    kh.setStatus(AccountStatus.KHOA);
    kh.setTokenVersion(kh.getTokenVersion() + 1);
    kh.setResetTokenHash(null);
    kh.setResetTokenExpiresAt(null);
  }
}
