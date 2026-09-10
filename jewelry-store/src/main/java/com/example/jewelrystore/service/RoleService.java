package com.example.jewelrystore.service;

import com.example.jewelrystore.entity.VaiTro;
import com.example.jewelrystore.entity.enums.DomainEnums.Role;

public interface RoleService {
  VaiTro get(Role role);
}
