package com.example.jewelrystore.security;

import com.example.jewelrystore.entity.enums.DomainEnums.Role;
import java.security.Principal;

public record Actor(Long id, Role role) implements Principal {
  public String getName() {
    return role + ":" + id;
  }

  public boolean customer() {
    return role == Role.KHACH_HANG;
  }
}
