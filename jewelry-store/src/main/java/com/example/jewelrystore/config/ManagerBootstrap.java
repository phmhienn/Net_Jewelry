package com.example.jewelrystore.config;

import com.example.jewelrystore.dto.request.StaffRequest;
import com.example.jewelrystore.entity.enums.DomainEnums.Role;
import com.example.jewelrystore.repository.TaiKhoanRepository;
import com.example.jewelrystore.service.StaffService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ManagerBootstrap implements CommandLineRunner {
  private final TaiKhoanRepository staff;
  private final StaffService service;
  private final Validator validator;

  @Value("${app.bootstrap.name}")
  private String name;

  @Value("${app.bootstrap.email}")
  private String email;

  @Value("${app.bootstrap.username}")
  private String username;

  @Value("${app.bootstrap.password}")
  private String password;

  @Override
  @Transactional
  public void run(String... args) {
    if (staff.countByAuthorityNameNot("KHACH_HANG") > 0) return;
    var request = new StaffRequest(name, email, null, username, password, Role.QUAN_LY);
    if (!validator.validate(request).isEmpty())
      throw new IllegalStateException(
          "Provide valid ADMIN_NAME, ADMIN_EMAIL, ADMIN_USERNAME and ADMIN_PASSWORD for manager bootstrap");
    service.create(request);
  }
}
