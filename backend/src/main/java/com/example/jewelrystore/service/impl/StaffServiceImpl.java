package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.StaffService;
import com.example.jewelrystore.util.Pages;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements StaffService {
  private void managementLock() {
    var all = staff.lockAll();
    require(
        all.stream()
            .anyMatch(
                nv ->
                    nv.getId().equals(actor.get().id())
                        && nv.getRole() == Role.QUAN_LY
                        && nv.getStatus() == AccountStatus.HOAT_DONG),
        "Tài khoản không còn quyền quản lý");
  }

  private final TaiKhoanRepository staff;
  private final com.example.jewelrystore.service.RoleService roles;
  private final UserMapper userMapper;
  private final PasswordEncoder passwords;
  private final CurrentActor actor;

  @Transactional(readOnly = true)
  public PageResponse<StaffResponse> list(String keyword, int page, int size) {
    return PageResponse.of(
        staff
            .findAll(
                (root, query, cb) -> {
                  var staffRole = cb.notEqual(root.get("authority").get("name"), "KHACH_HANG");
                  if (keyword == null || keyword.isBlank()) return staffRole;
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
                      staffRole,
                      cb.or(
                          cb.like(cb.lower(root.get("name")), k, '!'),
                          cb.like(cb.lower(root.get("email")), k, '!'),
                          cb.like(cb.lower(root.get("phone")), k, '!'),
                          cb.like(cb.lower(root.get("username")), k, '!')));
                },
                Pages.of(page, size))
            .map(userMapper::staff));
  }

  @Transactional(readOnly = true)
  public StaffResponse get(Long id) {
    var nv = com.example.jewelrystore.util.Checks.get(staff, id);
    require(nv.getRole() != Role.KHACH_HANG, "Tài khoản không phải nhân viên");
    return userMapper.staff(nv);
  }

  public StaffResponse create(StaffRequest request) {
    require(request.role() != Role.KHACH_HANG, "Vai trò nhân viên không hợp lệ");
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    require(
        !staff.existsByEmail(email) && !staff.existsByUsername(request.username()),
        "Email hoặc tên đăng nhập đã tồn tại");
    password(request.password());
    TaiKhoan nv = new TaiKhoan();
    nv.setName(request.name());
    nv.setEmail(email);
    nv.setPhone(request.phone());
    nv.setUsername(request.username());
    nv.setPassword(passwords.encode(request.password()));
    nv.setAuthority(roles.get(request.role()));
    return userMapper.staff(staff.save(nv));
  }

  public StaffResponse update(Long id, StaffUpdateRequest request) {
    TaiKhoan nv = lock(staff, id);
    require(nv.getRole() != Role.KHACH_HANG, "Tài khoản không phải nhân viên");
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    if (!nv.getEmail().equals(email)) require(!staff.existsByEmail(email), "Email đã tồn tại");
    nv.setName(request.name());
    nv.setEmail(email);
    nv.setPhone(request.phone());
    return userMapper.staff(nv);
  }

  public StaffResponse role(Long id, RoleRequest request) {
    managementLock();
    require(request.role() != Role.KHACH_HANG, "Vai trò nhân viên không hợp lệ");
    require(!id.equals(actor.get().id()), "Không được tự thay đổi vai trò");
    TaiKhoan nv = lock(staff, id);
    require(nv.getRole() != Role.KHACH_HANG, "Tài khoản không phải nhân viên");
    nv.setAuthority(roles.get(request.role()));
    nv.setTokenVersion(nv.getTokenVersion() + 1);
    return userMapper.staff(nv);
  }

  public StaffResponse status(Long id, AccountStatusRequest request) {
    managementLock();
    require(!id.equals(actor.get().id()), "Không được tự khóa tài khoản");
    TaiKhoan nv = lock(staff, id);
    require(nv.getRole() != Role.KHACH_HANG, "Tài khoản không phải nhân viên");
    nv.setStatus(request.status());
    nv.setTokenVersion(nv.getTokenVersion() + 1);
    return userMapper.staff(nv);
  }

  public void delete(Long id) {
    status(id, new AccountStatusRequest(AccountStatus.KHOA));
  }
}
