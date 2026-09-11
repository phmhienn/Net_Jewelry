package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.exception.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.*;
import com.example.jewelrystore.service.AuthService;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
  private final TaiKhoanRepository customers;
  private final com.example.jewelrystore.service.RoleService roles;
  private final GioHangRepository carts;
  private final PasswordEncoder passwords;
  private final JwtTokenProvider jwt;
  private final CustomUserDetailsService userDetails;
  private final CurrentActor actor;
  private final UserMapper userMapper;
  private final JavaMailSender mail;

  @Value("${app.reset.mail-enabled}")
  private boolean mailEnabled;

  @Value("${app.reset.frontend-url}")
  private String frontend;

  @Value("${app.mail.from}")
  private String from;

  private final String dummyHash =
      new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
          .encode("unused-login-timing-value");

  public UserResponse register(RegisterRequest request) {
    String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
    require(!customers.existsByEmail(email), "Email đã được sử dụng");
    password(request.password());
    TaiKhoan kh = new TaiKhoan();
    kh.setName(request.name().trim());
    kh.setEmail(email);
    String username =
        request.username() == null || request.username().isBlank()
            ? "kh_" + java.util.UUID.randomUUID().toString().replace("-", "")
            : request.username().trim();
    require(!customers.existsByUsername(username), "Tên đăng nhập đã được sử dụng");
    kh.setUsername(username);
    kh.setAuthority(roles.get(Role.KHACH_HANG));
    kh.setPassword(passwords.encode(request.password()));
    customers.save(kh);
    var cart = new GioHang();
    cart.setCustomer(kh);
    carts.save(cart);
    return userMapper.user(kh);
  }

  public LoginResponse login(LoginRequest request) {
    TaiKhoan tk = userDetails.findAccount(request.identifier()).orElse(null);
    verify(tk, request.password());
    return new LoginResponse(jwt.issue(tk, tk.getRole()), "Bearer", jwt.ttl(), userMapper.user(tk));
  }

  public LoginResponse staffLogin(StaffLoginRequest request) {
    TaiKhoan nv = userDetails.findAccount(request.username()).orElse(null);
    verify(nv, request.password());
    if (nv.getRole() == Role.KHACH_HANG)
      throw new UnauthorizedException("Tài khoản không phải nhân viên");
    return new LoginResponse(jwt.issue(nv, nv.getRole()), "Bearer", jwt.ttl(), userMapper.user(nv));
  }

  private void verify(TaiKhoan account, String password) {
    boolean valid =
        passwords.matches(password, account == null ? dummyHash : account.getPassword());
    if (account == null || !valid || account.getStatus() != AccountStatus.HOAT_DONG)
      throw new UnauthorizedException("Thông tin đăng nhập không đúng hoặc tài khoản đã khóa");
  }

  private TaiKhoan currentLocked() {
    var currentActor = actor.get();
    return lock(customers, currentActor.id());
  }

  public UserResponse me() {
    var currentActor = actor.get();
    return userMapper.user(get(customers, currentActor.id()));
  }

  public void logout() {
    TaiKhoan account = currentLocked();
    account.setTokenVersion(account.getTokenVersion() + 1);
  }

  public void changePassword(PasswordRequest request) {
    TaiKhoan account = currentLocked();
    verify(account, request.currentPassword());
    password(request.newPassword());
    account.setPassword(passwords.encode(request.newPassword()));
    account.setTokenVersion(account.getTokenVersion() + 1);
    {
      TaiKhoan kh = account;
      kh.setResetTokenHash(null);
      kh.setResetTokenExpiresAt(null);
    }
  }

  public void forgotPassword(ForgotPasswordRequest request) {
    if (!mailEnabled) throw new ApiException(503, "Chức năng email chưa được cấu hình");
    customers
        .findByEmail(request.email().trim().toLowerCase(java.util.Locale.ROOT))
        .ifPresent(
            found -> {
              TaiKhoan kh = lock(customers, found.getId());
              if (kh.getStatus() != AccountStatus.HOAT_DONG) return;
              byte[] random = new byte[32];
              new SecureRandom().nextBytes(random);
              String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
              kh.setResetTokenHash(hash(token));
              kh.setResetTokenExpiresAt(Instant.now().plusSeconds(1800));
              var message = new SimpleMailMessage();
              message.setFrom(from);
              message.setTo(kh.getEmail());
              message.setSubject("Đặt lại mật khẩu Jewelry Store");
              message.setText(
                  "Liên kết có hiệu lực 30 phút: " + frontend + "/reset-password?token=" + token);
              mail.send(message);
            });
  }

  public void resetPassword(ResetPasswordRequest request) {
    var found =
        customers
            .findByResetTokenHash(hash(request.token()))
            .orElseThrow(() -> new BadRequestException("Liên kết không hợp lệ hoặc đã hết hạn"));
    TaiKhoan kh = lock(customers, found.getId());
    require(
        hash(request.token()).equals(kh.getResetTokenHash())
            && kh.getResetTokenExpiresAt() != null
            && kh.getResetTokenExpiresAt().isAfter(Instant.now())
            && kh.getStatus() == AccountStatus.HOAT_DONG,
        "Liên kết không hợp lệ hoặc đã hết hạn");
    password(request.newPassword());
    kh.setPassword(passwords.encode(request.newPassword()));
    kh.setTokenVersion(kh.getTokenVersion() + 1);
    kh.setResetTokenHash(null);
    kh.setResetTokenExpiresAt(null);
  }
}
