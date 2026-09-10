package com.example.jewelrystore.service.impl;

import com.example.jewelrystore.entity.VaiTro;
import com.example.jewelrystore.entity.enums.DomainEnums.Role;
import com.example.jewelrystore.repository.VaiTroRepository;
import com.example.jewelrystore.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
  private final VaiTroRepository roles;

  public VaiTro get(Role role) {
    return roles
        .findByName(role.name())
        .orElseThrow(() -> new IllegalStateException("Thiếu vai trò trong database: " + role));
  }
}
